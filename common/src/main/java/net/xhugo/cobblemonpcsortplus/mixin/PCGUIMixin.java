package net.xhugo.cobblemonpcsortplus.mixin;

import com.cobblemon.mod.common.client.gui.pc.BoxNameWidget;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.xhugo.cobblemonpcsortplus.client.CpsButton;
import net.xhugo.cobblemonpcsortplus.client.CpsGui;
import net.xhugo.cobblemonpcsortplus.client.CpsKeybinds;
import net.xhugo.cobblemonpcsortplus.client.CpsOverlayLayout;
import net.xhugo.cobblemonpcsortplus.client.TagPanelWidget;
import net.xhugo.cobblemonpcsortplus.client.TagToggleWidget;
import net.xhugo.cobblemonpcsortplus.network.ClientNetworking;
import net.xhugo.cobblemonpcsortplus.network.ClientProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overlay widgets on Cobblemon's PC screen. The PC itself is never moved or rebuilt;
 * only our toolbar and tag panel are added after {@code init}.
 */
@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {
    @Unique private static final List<List<String>> CPS_TAG_PAGES = List.of(
            List.of(
                    "SOURCE", "PROTECTED", "FALLBACK", "HIGH_IV", "SHINY",
                    "ALPHA", "LEGENDARY", "MYTHICAL", "WATER", "FIRE",
                    "GRASS", "ELECTRIC", "STEEL", "GROUND",
                    "ROCK", "FIGHTING", "PSYCHIC", "FLYING",
                    "BUG", "POISON", "DARK", "DRAGON",
                    "ICE", "FAIRY", "NORMAL", "GHOST"
            ),
            List.of(
                    "MALE", "FEMALE", "HIDDEN_ABILITY", "HELD_ITEM",
                    "FOSSIL", "PSEUDO_LEGENDARY", "ULTRA_BEAST", "PARADOX", "BABY", "REGIONAL",
                    "LEVEL_1", "LEVEL_100",
                    "GEN_1", "GEN_2", "GEN_3", "GEN_4", "GEN_5", "GEN_6", "GEN_7", "GEN_8", "GEN_9",
                    "SIZE_XS", "SIZE_S", "SIZE_M", "SIZE_L", "SIZE_XL"
            )
    );
    @Unique private static final int CPS_TAG_WIDTH = CpsGui.TAG_WIDTH;
    @Unique private static final int CPS_TAG_HEIGHT = CpsGui.CHIP_HEIGHT;
    @Unique private static final int CPS_TAG_COL_STRIDE = CpsGui.TAG_WIDTH + CpsGui.TAG_COL_GAP;
    @Unique private static final int CPS_TAG_ROW_STRIDE = CpsGui.CHIP_HEIGHT + CpsGui.CHIP_GAP_Y;
    @Unique private static final int CPS_BUTTON_WIDTH = 81;
    @Unique private static final int CPS_BUTTON_GAP = 8;
    @Unique private static final int CPS_TOOLBAR_HEIGHT = CpsGui.BUTTON_HEIGHT;
    @Unique private static final int CPS_TOOLBAR_BUTTONS = 4;

    @Shadow(remap = false)
    public abstract StorageWidget getStorage();

    @Shadow(remap = false)
    public abstract ClientPC getPc();

    @Unique private CpsButton cps$sortButton;
    @Unique private CpsButton cps$sortAllButton;
    @Unique private CpsButton cps$tagsToggleButton;
    @Unique private CpsButton cps$undoButton;
    @Unique private CpsButton cps$ivThresholdButton;
    @Unique private CpsButton cps$prevPageButton;
    @Unique private CpsButton cps$nextPageButton;
    @Unique private CpsButton cps$boxSortModeButton;
    @Unique private CpsButton cps$clearTagsButton;
    @Unique private TagPanelWidget cps$tagPanel;
    @Unique private final Map<String, TagToggleWidget> cps$tagButtons = new LinkedHashMap<>();
    @Unique private final Map<String, Integer> cps$tagPage = new LinkedHashMap<>();
    @Unique private boolean cps$tagsOpen;
    @Unique private int cps$page;
    @Unique private int cps$lastBox = -1;
    @Unique private int cps$lastProfileRevision = -1;

    protected PCGUIMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cps$addControls(CallbackInfo callback) {
        int toolbarWidth = CPS_BUTTON_WIDTH * CPS_TOOLBAR_BUTTONS + CPS_BUTTON_GAP * (CPS_TOOLBAR_BUTTONS - 1);
        CpsOverlayLayout layout = CpsOverlayLayout.compute(
                width,
                height,
                PCGUI.BASE_WIDTH,
                PCGUI.BASE_HEIGHT,
                CpsGui.PANEL_WIDTH,
                CpsGui.PANEL_HEIGHT,
                toolbarWidth,
                CPS_TOOLBAR_HEIGHT
        );
        float panelScale = layout.panelScale();
        int panelX = layout.panelX();
        int panelY = layout.panelY();
        int toolbarX = layout.toolbarX();
        int controlsY = layout.toolbarY();

        cps$tagsToggleButton = addRenderableWidget(new CpsButton(
                toolbarX, controlsY, CPS_BUTTON_WIDTH, CPS_TOOLBAR_HEIGHT,
                Component.translatable("screen.cobblemonpcsortplus.tags_closed"),
                () -> {
                    cps$tagsOpen = !cps$tagsOpen;
                    cps$setTagPanelVisible(cps$tagsOpen);
                }
        ));
        cps$tagsToggleButton.setTooltip(Tooltip.create(Component.translatable("screen.cobblemonpcsortplus.tags.tooltip")));

        cps$sortButton = addRenderableWidget(new CpsButton(
                toolbarX + CPS_BUTTON_WIDTH + CPS_BUTTON_GAP, controlsY, CPS_BUTTON_WIDTH, CPS_TOOLBAR_HEIGHT,
                Component.translatable("screen.cobblemonpcsortplus.sort"),
                ClientNetworking::requestSort
        ));

        cps$sortAllButton = addRenderableWidget(new CpsButton(
                toolbarX + (CPS_BUTTON_WIDTH + CPS_BUTTON_GAP) * 2, controlsY, CPS_BUTTON_WIDTH, CPS_TOOLBAR_HEIGHT,
                Component.translatable("screen.cobblemonpcsortplus.sort_all"),
                ClientNetworking::requestSortAll
        ));

        cps$undoButton = addRenderableWidget(new CpsButton(
                toolbarX + (CPS_BUTTON_WIDTH + CPS_BUTTON_GAP) * 3, controlsY, CPS_BUTTON_WIDTH, CPS_TOOLBAR_HEIGHT,
                Component.translatable("screen.cobblemonpcsortplus.undo"),
                ClientNetworking::requestUndo
        ));

        cps$tagPanel = addRenderableOnly(new TagPanelWidget(
                panelX, panelY, panelScale,
                this::cps$boxTitle,
                () -> cps$page,
                () -> ClientProfileCache.boxHasTags(getStorage().getBox() + 1),
                CPS_TAG_PAGES.size()
        ));

        cps$ivThresholdButton = addRenderableWidget(new CpsButton(
                layout.panelChildX(CpsGui.PANEL_WIDTH - CpsGui.TAG_INSET - CpsGui.IV_WIDTH),
                layout.panelChildY(CpsGui.IV_PAD),
                CpsGui.IV_WIDTH,
                CPS_TOOLBAR_HEIGHT,
                Component.empty(),
                () -> {
                    int currentBox = getStorage().getBox() + 1;
                    int threshold = ClientProfileCache.cycleHighIvThreshold(currentBox);
                    ClientNetworking.updateHighIvThreshold(currentBox, threshold);
                    cps$refreshTagControls();
                }
        ));
        cps$ivThresholdButton.setTooltip(Tooltip.create(
                Component.translatable("screen.cobblemonpcsortplus.high_iv_threshold.tooltip")
        ));
        cps$ivThresholdButton.setDrawScale(panelScale);

        cps$prevPageButton = addRenderableWidget(new CpsButton(
                layout.panelChildX(CpsGui.TAG_INSET),
                layout.panelChildY(CpsGui.PAGER_Y),
                CpsGui.PAGER_SIZE,
                CpsGui.PAGER_SIZE,
                Component.literal("<"),
                () -> cps$setPage(cps$page - 1)
        ));
        cps$prevPageButton.setTooltip(Tooltip.create(Component.translatable("screen.cobblemonpcsortplus.tags_prev.tooltip")));
        cps$prevPageButton.setDrawScale(panelScale);

        cps$nextPageButton = addRenderableWidget(new CpsButton(
                layout.panelChildX(CpsGui.PANEL_WIDTH - CpsGui.TAG_INSET - CpsGui.PAGER_SIZE),
                layout.panelChildY(CpsGui.PAGER_Y),
                CpsGui.PAGER_SIZE,
                CpsGui.PAGER_SIZE,
                Component.literal(">"),
                () -> cps$setPage(cps$page + 1)
        ));
        cps$nextPageButton.setTooltip(Tooltip.create(Component.translatable("screen.cobblemonpcsortplus.tags_next.tooltip")));
        cps$nextPageButton.setDrawScale(panelScale);

        for (int page = 0; page < CPS_TAG_PAGES.size(); page++) {
            List<String> tags = CPS_TAG_PAGES.get(page);
            for (int index = 0; index < tags.size(); index++) {
                String tag = tags.get(index);
                int column = index % 2;
                int row = index / 2;
                TagToggleWidget tagButton = addRenderableWidget(new TagToggleWidget(
                        layout.panelChildX(CpsGui.TAG_INSET + (column * CPS_TAG_COL_STRIDE)),
                        layout.panelChildY(CpsGui.TAG_START_Y + (row * CPS_TAG_ROW_STRIDE)),
                        CPS_TAG_WIDTH,
                        CPS_TAG_HEIGHT,
                        Component.translatable(cps$tagKey(tag) + ".short"),
                        CpsGui.typeForTag(tag),
                        () -> cps$toggleTag(tag)
                ));
                tagButton.setTooltip(Tooltip.create(Component.translatable(cps$tagKey(tag))));
                tagButton.setDrawScale(panelScale);
                cps$tagButtons.put(tag, tagButton);
                cps$tagPage.put(tag, page);
            }
        }

        cps$clearTagsButton = addRenderableWidget(new CpsButton(
                layout.panelChildX(CpsGui.TAG_INSET),
                layout.panelChildY(CpsGui.PANEL_HEIGHT - CpsGui.BUTTON_HEIGHT),
                CpsGui.CLEAR_WIDTH,
                CPS_TOOLBAR_HEIGHT,
                Component.translatable("screen.cobblemonpcsortplus.clear_tags"),
                this::cps$clearTags
        ));
        cps$clearTagsButton.setTooltip(Tooltip.create(
                Component.translatable("screen.cobblemonpcsortplus.clear_tags.tooltip")
        ));
        cps$clearTagsButton.setDrawScale(panelScale);

        cps$boxSortModeButton = addRenderableWidget(new CpsButton(
                layout.panelChildX(CpsGui.TAG_INSET + CpsGui.CLEAR_WIDTH + CpsGui.TAG_COL_GAP),
                layout.panelChildY(CpsGui.PANEL_HEIGHT - CpsGui.BUTTON_HEIGHT),
                CpsGui.ORDER_WIDTH,
                CPS_TOOLBAR_HEIGHT,
                Component.empty(),
                () -> {
                    var mode = ClientProfileCache.cycleFinalSortMode();
                    ClientNetworking.updateBoxSortMode(mode.serializedName());
                    cps$refreshTagControls();
                }
        ));
        cps$boxSortModeButton.setTooltip(Tooltip.create(
                Component.translatable("screen.cobblemonpcsortplus.box_order.tooltip")
        ));
        cps$boxSortModeButton.setDrawScale(panelScale);

        cps$setTagPanelVisible(false);
        ClientNetworking.requestProfile();
        cps$refreshTagControls();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cps$refreshOnRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo callback) {
        int box = getStorage().getBox();
        if (box != cps$lastBox || ClientProfileCache.revision() != cps$lastProfileRevision) {
            cps$refreshTagControls();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cps$onKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
        if (cps$textFieldFocused()) return;
        if (CpsKeybinds.matches(CpsKeybinds.SORT, keyCode, scanCode)) {
            CpsGui.playClick();
            ClientNetworking.requestSort();
            callback.setReturnValue(true);
            return;
        }
        if (CpsKeybinds.matches(CpsKeybinds.SORT_ALL, keyCode, scanCode)) {
            CpsGui.playClick();
            ClientNetworking.requestSortAll();
            callback.setReturnValue(true);
            return;
        }
        if (CpsKeybinds.matches(CpsKeybinds.UNDO, keyCode, scanCode) && ClientProfileCache.canUndo()) {
            CpsGui.playClick();
            ClientNetworking.requestUndo();
            callback.setReturnValue(true);
        }
    }

    @Unique
    private Component cps$boxTitle() {
        int index = getStorage().getBox();
        String live = cps$liveBoxName();
        if (live != null && !live.isBlank()) {
            return Component.literal(live);
        }
        return Component.translatable("screen.cobblemonpcsortplus.box_title", Math.max(1, index + 1));
    }

    /**
     * Prefers the name the player is currently typing, then the stored box name.
     * Cobblemon names carry a style component; we take the plain string so the
     * overlay can apply its own bold title.
     */
    @Unique
    private String cps$liveBoxName() {
        for (var child : children()) {
            if (child instanceof BoxNameWidget widget && widget.isFocused()) {
                String typed = widget.getValue();
                if (typed != null && !typed.isBlank()) return typed;
                return null;
            }
        }
        int index = getStorage().getBox();
        var boxes = getPc().getBoxes();
        if (index < 0 || index >= boxes.size()) return null;
        var name = boxes.get(index).getName();
        if (name == null) return null;
        String plain = name.getString();
        return plain.isBlank() ? null : plain;
    }

    @Unique
    private void cps$toggleTag(String tag) {
        if (!ClientProfileCache.isLoaded()) return;
        int currentBox = getStorage().getBox() + 1;
        Set<String> tags = ClientProfileCache.toggleTag(currentBox, tag);
        ClientNetworking.updateBoxTags(currentBox, tags);
        cps$refreshTagControls();
    }

    @Unique
    private void cps$setPage(int page) {
        cps$page = Math.max(0, Math.min(CPS_TAG_PAGES.size() - 1, page));
        cps$refreshTagControls();
    }

    @Unique
    private void cps$setTagPanelVisible(boolean visible) {
        cps$tagPanel.visible = visible;
        cps$ivThresholdButton.visible = visible;
        cps$ivThresholdButton.active = visible && ClientProfileCache.isLoaded();
        cps$prevPageButton.visible = visible;
        cps$nextPageButton.visible = visible;
        cps$clearTagsButton.visible = visible;
        cps$clearTagsButton.active = visible && ClientProfileCache.boxHasTags(getStorage().getBox() + 1);
        cps$boxSortModeButton.visible = visible;
        cps$boxSortModeButton.active = visible && ClientProfileCache.isLoaded();
        cps$tagButtons.forEach((tag, button) -> {
            boolean onPage = visible && cps$tagPage.getOrDefault(tag, 0) == cps$page;
            button.visible = onPage;
            button.active = onPage && ClientProfileCache.isLoaded();
        });
        cps$tagsToggleButton.setMessage(Component.translatable(
                visible ? "screen.cobblemonpcsortplus.tags_open" : "screen.cobblemonpcsortplus.tags_closed"
        ));
        if (visible) cps$refreshPagerButtons();
    }

    @Unique
    private void cps$clearTags() {
        if (!ClientProfileCache.isLoaded()) return;
        int currentBox = getStorage().getBox() + 1;
        Set<String> tags = ClientProfileCache.clearTags(currentBox);
        ClientNetworking.updateBoxTags(currentBox, tags);
        cps$refreshTagControls();
    }

    @Unique
    private void cps$refreshTagControls() {
        int currentBox = getStorage().getBox() + 1;
        boolean loaded = ClientProfileCache.isLoaded();
        Set<String> assigned = ClientProfileCache.tagsForBox(currentBox);
        for (Map.Entry<String, TagToggleWidget> entry : cps$tagButtons.entrySet()) {
            boolean onPage = cps$tagsOpen && cps$tagPage.getOrDefault(entry.getKey(), 0) == cps$page;
            entry.getValue().setSelected(loaded && assigned.contains(entry.getKey()));
            entry.getValue().visible = onPage;
            entry.getValue().active = onPage && loaded;
        }
        cps$ivThresholdButton.setMessage(Component.literal(
                loaded ? "IV ≥ " + ClientProfileCache.highIvThreshold(currentBox) + "/6" : "IV: ..."
        ));
        cps$ivThresholdButton.active = cps$tagsOpen && loaded;
        cps$refreshPagerButtons();
        var mode = ClientProfileCache.finalSortMode();
        cps$boxSortModeButton.setMessage(Component.translatable(
                "screen.cobblemonpcsortplus.box_order",
                Component.translatable("screen.cobblemonpcsortplus.box_order." + mode.translationSuffix())
        ));
        cps$boxSortModeButton.active = cps$tagsOpen && loaded;
        boolean tagged = loaded && !assigned.isEmpty();
        cps$clearTagsButton.active = cps$tagsOpen && tagged;
        cps$tagsToggleButton.setSelected(tagged);
        cps$tagsToggleButton.setMessage(Component.translatable(
                cps$tagsOpen ? "screen.cobblemonpcsortplus.tags_open" : "screen.cobblemonpcsortplus.tags_closed"
        ));
        cps$undoButton.active = ClientProfileCache.canUndo();
        cps$sortButton.setTooltip(Tooltip.create(CpsKeybinds.withShortcut("screen.cobblemonpcsortplus.sort.tooltip", CpsKeybinds.SORT)));
        cps$sortAllButton.setTooltip(Tooltip.create(CpsKeybinds.withShortcut("screen.cobblemonpcsortplus.sort_all.tooltip", CpsKeybinds.SORT_ALL)));
        cps$undoButton.setTooltip(Tooltip.create(CpsKeybinds.withShortcut("screen.cobblemonpcsortplus.undo.tooltip", CpsKeybinds.UNDO)));
        cps$lastBox = getStorage().getBox();
        cps$lastProfileRevision = ClientProfileCache.revision();
    }

    @Unique
    private void cps$refreshPagerButtons() {
        boolean open = cps$tagsOpen;
        cps$prevPageButton.visible = open;
        cps$prevPageButton.active = open && cps$page > 0;
        cps$nextPageButton.visible = open;
        cps$nextPageButton.active = open && cps$page < CPS_TAG_PAGES.size() - 1;
    }

    /** Skip shortcuts while Cobblemon's box-name or filter field has focus. */
    @Unique
    private boolean cps$textFieldFocused() {
        var focused = getFocused();
        if (focused == null) return false;
        String simpleName = focused.getClass().getSimpleName();
        return "BoxNameWidget".equals(simpleName) || "FilterWidget".equals(simpleName);
    }

    @Unique
    private static String cps$tagKey(String tag) {
        return "tag.cobblemonpcsortplus." + tag.toLowerCase(Locale.ROOT);
    }
}
