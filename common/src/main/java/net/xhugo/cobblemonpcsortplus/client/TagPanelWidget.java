package net.xhugo.cobblemonpcsortplus.client;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Baked panel chrome. Child widgets live in native panel space and scale with it. */
public final class TagPanelWidget extends AbstractWidget {
    private final Supplier<Component> boxTitle;
    private final IntSupplier currentPage;
    private final BooleanSupplier tagged;
    private final int pageCount;
    private float scale = 1F;

    public TagPanelWidget(
            int x,
            int y,
            float scale,
            Supplier<Component> boxTitle,
            IntSupplier currentPage,
            BooleanSupplier tagged,
            int pageCount
    ) {
        super(
                x,
                y,
                Math.max(1, Math.round(CpsGui.PANEL_WIDTH * CpsDrawScale.sanitize(scale))),
                Math.max(1, Math.round(CpsGui.PANEL_HEIGHT * CpsDrawScale.sanitize(scale))),
                Component.empty()
        );
        this.boxTitle = boxTitle;
        this.currentPage = currentPage;
        this.tagged = tagged;
        this.pageCount = pageCount;
        this.scale = CpsDrawScale.sanitize(scale);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        CpsDrawScale.begin(graphics, this, scale);
        CpsGui.blit(graphics, CpsGui.PANEL, 0, 0, CpsGui.PANEL_WIDTH, CpsGui.PANEL_HEIGHT);
        CpsGui.drawBoxTitle(graphics, boxTitle.get(), 0, 0, tagged.getAsBoolean());
        CpsGui.drawPageDots(graphics, 0, 0, currentPage.getAsInt(), pageCount);
        CpsDrawScale.end(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        // The individual tag buttons provide narration.
    }
}
