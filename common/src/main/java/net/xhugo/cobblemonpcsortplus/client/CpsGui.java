package net.xhugo.cobblemonpcsortplus.client;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.client.gui.TypeIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Shared sprites, overlay metrics, and drawing helpers. */
public final class CpsGui {
    public static final ResourceLocation PANEL = sprite("panel/background");
    public static final ResourceLocation BUTTON = sprite("widget/button");
    public static final ResourceLocation BUTTON_HOVER = sprite("widget/button_highlighted");
    public static final ResourceLocation BUTTON_DISABLED = sprite("widget/button_disabled");
    public static final ResourceLocation BUTTON_SELECTED = sprite("widget/button_selected");
    public static final ResourceLocation BUTTON_SELECTED_HOVER = sprite("widget/button_selected_highlighted");

    public static final int BUTTON_HEIGHT = 20;
    public static final int IV_PAD = 6;
    public static final int IV_WIDTH = 56;
    public static final int CHIP_HEIGHT = 16;
    public static final int CHIP_GAP_Y = 3;
    public static final int TAG_ROWS = 13;
    public static final int TAG_WIDTH = 74;
    public static final int TAG_COL_GAP = 6;
    public static final int TAG_INSET = 10;
    public static final int PAGER_SIZE = 16;
    public static final int PAGER_GAP = 4;
    public static final int PAGER_Y = IV_PAD + BUTTON_HEIGHT + 2 + 4;
    public static final int TAG_START_Y = PAGER_Y + PAGER_SIZE + PAGER_GAP;
    public static final int TAG_BOTTOM_PAD = 6;
    public static final int CLEAR_WIDTH = TAG_WIDTH;
    public static final int ORDER_WIDTH = TAG_WIDTH;
    public static final int FOOTER_HEIGHT = 4 + BUTTON_HEIGHT;
    public static final int PANEL_WIDTH = TAG_INSET * 2 + TAG_WIDTH * 2 + TAG_COL_GAP;
    public static final int PANEL_HEIGHT = TAG_START_Y
            + TAG_ROWS * (CHIP_HEIGHT + CHIP_GAP_Y)
            - CHIP_GAP_Y
            + TAG_BOTTOM_PAD
            + FOOTER_HEIGHT;
    private static final int PIP = 0xFF3D3D3D;
    private static final int PIP_TAGGED = 0xFF2F7A8A;
    private static final int TITLE = 0xFFFFFF;
    private static final int TITLE_TAGGED = 0xFF55C6E8;
    private static final int PIP_SIZE = 2;
    private static final int PIP_STRIDE = 5;
    private static final int PIP_STAGGER = 3;
    private static final int PIP_COLS = 4;
    private static final int PIP_COLS_MIN = 3;
    private static final int PIP_ROWS = 2;
    private static final int PIP_TEXT_PAD = 6;
    private static final int DOT = 0xFFC6C6C6;
    private static final int DOT_IDLE = 0xFF4B4B4B;
    private static final int DOT_SIZE = 3;
    private static final int DOT_GAP = 5;

    private CpsGui() {
    }

    public static ResourceLocation buttonSprite(boolean active, boolean selected, boolean hovered) {
        if (!active) {
            return BUTTON_DISABLED;
        }
        if (selected) {
            return hovered ? BUTTON_SELECTED_HOVER : BUTTON_SELECTED;
        }
        return hovered ? BUTTON_HOVER : BUTTON;
    }

    public static ResourceLocation chipSprite(boolean selected, boolean hovered) {
        return buttonSprite(true, selected, hovered);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    public static void drawVanillaLabel(GuiGraphics graphics, Component text, int x, int y, int width, int height, boolean active) {
        int color = active ? 0xFFFFFF : 0xA0A0A0;
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                text,
                x + width / 2,
                y + (height - 8) / 2,
                color
        );
    }

    public static void drawBoxTitle(GuiGraphics graphics, Component text, int panelX, int panelY, boolean tagged) {
        var font = Minecraft.getInstance().font;
        String raw = text.getString();
        Component drawn = bold(raw);
        int textWidth = font.width(drawn);
        int textY = panelY + IV_PAD + (BUTTON_HEIGHT - 8) / 2;
        int headerLeft = panelX + 8;
        int headerRight = panelX + PANEL_WIDTH - TAG_INSET - IV_WIDTH - 4;
        int innerWidth = Math.max(0, headerRight - headerLeft);

        int cols = PIP_COLS;
        int clusterWidth = pipClusterWidth(cols);
        int reserved = clusterWidth * 2 + PIP_TEXT_PAD * 2;
        // Long names drop to 3 columns per cluster so the pips still read as a pair of groups.
        if (reserved + textWidth > innerWidth) {
            cols = PIP_COLS_MIN;
            clusterWidth = pipClusterWidth(cols);
            reserved = clusterWidth * 2 + PIP_TEXT_PAD * 2;
        }
        int maxTextWidth = Math.max(0, innerWidth - reserved);
        if (textWidth > maxTextWidth) {
            cols = PIP_COLS_MIN;
            clusterWidth = pipClusterWidth(cols);
            reserved = clusterWidth * 2 + PIP_TEXT_PAD * 2;
            maxTextWidth = Math.max(0, innerWidth - reserved);
            drawn = ellipsizeBold(font, raw, maxTextWidth);
            textWidth = font.width(drawn);
        }
        int totalWidth = reserved + textWidth;
        int startX = headerLeft + Math.max(0, (innerWidth - totalWidth) / 2);
        int clusterHeight = pipClusterHeight(PIP_ROWS);
        int pipY = textY + (8 - clusterHeight) / 2;
        int pipColor = tagged ? PIP_TAGGED : PIP;
        drawStaggeredPips(graphics, startX, pipY, cols, PIP_ROWS, true, pipColor);
        int textX = startX + clusterWidth + PIP_TEXT_PAD;
        graphics.drawString(font, drawn, textX, textY, tagged ? TITLE_TAGGED : TITLE, true);
        drawStaggeredPips(graphics, textX + textWidth + PIP_TEXT_PAD, pipY, cols, PIP_ROWS, false, pipColor);
    }

    public static void drawPageDots(GuiGraphics graphics, int panelX, int panelY, int page, int pageCount) {
        int total = pageCount * DOT_SIZE + Math.max(0, pageCount - 1) * DOT_GAP;
        int x = panelX + (PANEL_WIDTH - total) / 2;
        int y = panelY + PAGER_Y + (PAGER_SIZE - DOT_SIZE) / 2;
        for (int index = 0; index < pageCount; index++) {
            int color = index == page ? DOT : DOT_IDLE;
            graphics.fill(x, y, x + DOT_SIZE, y + DOT_SIZE, color);
            x += DOT_SIZE + DOT_GAP;
        }
    }

    public static void drawTypeIcon(GuiGraphics graphics, ElementalType type, int x, int y) {
        new TypeIcon(x, y, type, null, false, true, 15F, 7.5F, 1F).render(graphics);
    }

    public static ElementalType typeForTag(String tag) {
        return ElementalTypes.get(tag.toLowerCase());
    }

    public static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, path);
    }

    private static Component bold(String text) {
        return Component.literal(text).withStyle(Style.EMPTY.withBold(true));
    }

    /**
     * Truncates with the same bold style used when drawing. Vanilla
     * {@code substrByWidth} measures unstyled glyphs and would clip the right pips.
     */
    private static Component ellipsizeBold(Font font, String raw, int maxWidth) {
        Component full = bold(raw);
        if (maxWidth <= 0) return bold("");
        if (font.width(full) <= maxWidth) return full;
        int budget = Math.max(0, maxWidth - font.width(bold("...")));
        int cut = raw.length();
        while (cut > 0 && font.width(bold(raw.substring(0, cut))) > budget) {
            cut--;
        }
        return bold(raw.substring(0, cut) + "...");
    }

    private static int pipClusterWidth(int cols) {
        return PIP_STAGGER + (cols - 1) * PIP_STRIDE + PIP_SIZE;
    }

    private static int pipClusterHeight(int rows) {
        return (rows - 1) * PIP_STRIDE + PIP_SIZE;
    }

    private static void drawStaggeredPips(
            GuiGraphics graphics,
            int x,
            int y,
            int cols,
            int rows,
            boolean topRowShiftedRight,
            int color
    ) {
        for (int row = 0; row < rows; row++) {
            boolean shiftRight = topRowShiftedRight ? row % 2 == 0 : row % 2 == 1;
            int offsetX = shiftRight ? PIP_STAGGER : 0;
            int pipY = y + row * PIP_STRIDE;
            for (int col = 0; col < cols; col++) {
                int pipX = x + offsetX + col * PIP_STRIDE;
                graphics.fill(pipX, pipY, pipX + PIP_SIZE, pipY + PIP_SIZE, color);
            }
        }
    }
}
