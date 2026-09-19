package net.xhugo.cobblemonpcsortplus.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpsOverlayLayoutTest {
    private static final int PC_W = 349;
    private static final int PC_H = 205;
    private static final int PANEL_W = 174;
    private static final int PANEL_H = 326;
    private static final int TOOLBAR_W = 348;
    private static final int TOOLBAR_H = 20;

    @Test
    void wideScreensKeepANativePanelOnTheRightWithoutMovingThePc() {
        CpsOverlayLayout layout = compute(1920, 1080);
        assertEquals((1920 - PC_W) / 2, layout.pcX());
        assertEquals((1080 - PC_H) / 2, layout.pcY());
        assertEquals(1F, layout.panelScale());
        assertEquals(layout.pcX() + PC_W + CpsOverlayLayout.GAP, layout.panelX());
    }

    @Test
    void guiScaleThreeOn1080pShrinksThePanelBesideThePc() {
        CpsOverlayLayout layout = compute(640, 360);
        assertEquals((640 - PC_W) / 2, layout.pcX());
        assertEquals((360 - PC_H) / 2, layout.pcY());
        assertTrue(layout.panelScale() < 1F);
        assertTrue(layout.panelScale() >= CpsOverlayLayout.MIN_SCALE);
        assertEquals(layout.pcX() + PC_W + CpsOverlayLayout.GAP, layout.panelX());
        assertTrue(layout.panelX() + layout.scaled(PANEL_W) <= 640 - CpsOverlayLayout.MARGIN);
    }

    @Test
    void aTinyGuiDoesNotMoveThePcEvenIfThePanelOverlays() {
        CpsOverlayLayout layout = compute(426, 240);
        assertEquals((426 - PC_W) / 2, layout.pcX());
        assertEquals((240 - PC_H) / 2, layout.pcY());
        assertTrue(layout.panelScale() > 0F);
        assertTrue(layout.panelX() >= CpsOverlayLayout.MARGIN);
    }

    private static CpsOverlayLayout compute(int width, int height) {
        return CpsOverlayLayout.compute(width, height, PC_W, PC_H, PANEL_W, PANEL_H, TOOLBAR_W, TOOLBAR_H);
    }
}
