package net.xhugo.cobblemonpcsortplus.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** 9-slice button using the Cobblemon Interface sprites. */
public final class CpsButton extends AbstractWidget {
    private final Runnable onPress;
    private final int nativeWidth;
    private final int nativeHeight;
    private boolean selected;
    private float scale = 1F;

    public CpsButton(int x, int y, int width, int height, Component label, Runnable onPress) {
        super(x, y, width, height, label);
        this.onPress = onPress;
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
        CpsGui.blit(graphics, CpsGui.buttonSprite(active, selected, isHovered()), 0, 0, nativeWidth, nativeHeight);
        CpsGui.drawVanillaLabel(graphics, getMessage(), 0, 0, nativeWidth, nativeHeight, active);
        CpsDrawScale.end(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active || button != 0 || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        CpsGui.playClick();
        onPress.run();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }
}
