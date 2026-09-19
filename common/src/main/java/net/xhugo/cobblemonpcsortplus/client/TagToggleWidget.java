package net.xhugo.cobblemonpcsortplus.client;

import com.cobblemon.mod.common.api.types.ElementalType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Button chrome from Cobblemon Interface. Selected tags use the teal sprite. */
public final class TagToggleWidget extends AbstractWidget {
    private final Runnable onToggle;
    private final ElementalType type;
    private final int nativeWidth;
    private final int nativeHeight;
    private boolean selected;
    private float scale = 1F;

    public TagToggleWidget(
            int x,
            int y,
            int width,
            int height,
            Component label,
            ElementalType type,
            Runnable onToggle
    ) {
        super(x, y, width, height, label);
        this.type = type;
        this.onToggle = onToggle;
        this.nativeWidth = width;
        this.nativeHeight = height;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setDrawScale(float scale) {
        this.scale = CpsDrawScale.sanitize(scale);
        CpsDrawScale.apply(this, nativeWidth, nativeHeight, this.scale);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        CpsDrawScale.begin(graphics, this, scale);
        CpsGui.blit(graphics, CpsGui.chipSprite(selected, isHovered()), 0, 0, nativeWidth, nativeHeight);
        if (type != null) {
            CpsGui.drawTypeIcon(graphics, type, 5, 3);
            CpsGui.drawVanillaLabel(graphics, getMessage(), 16, 0, nativeWidth - 20, nativeHeight, true);
        } else {
            CpsGui.drawVanillaLabel(graphics, getMessage(), 0, 0, nativeWidth, nativeHeight, true);
        }
        CpsDrawScale.end(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active || button != 0 || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        CpsGui.playClick();
        onToggle.run();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }
}
