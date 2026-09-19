package net.xhugo.cobblemonpcsortplus.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

/** Scales overlay widgets in place. Mouse hit boxes stay on the scaled bounds. */
final class CpsDrawScale {
    private CpsDrawScale() {
    }

    static float sanitize(float scale) {
        return scale <= 0F ? 1F : scale;
    }

    static void apply(AbstractWidget widget, int nativeWidth, int nativeHeight, float scale) {
        float safe = sanitize(scale);
        widget.setWidth(Math.max(1, Math.round(nativeWidth * safe)));
        widget.setHeight(Math.max(1, Math.round(nativeHeight * safe)));
    }

    static void begin(GuiGraphics graphics, AbstractWidget widget, float scale) {
        float safe = sanitize(scale);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(widget.getX(), widget.getY(), 0);
        pose.scale(safe, safe, 1F);
    }

    static void end(GuiGraphics graphics) {
        graphics.pose().popPose();
    }
}
