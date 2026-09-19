package net.xhugo.cobblemonpcsortplus.client;

/**
 * Places the tag overlay next to Cobblemon's PC without moving the PC.
 * When the GUI scale leaves no native-size strip, the panel is scaled in
 * widget space (hit boxes included). The PC origin always matches
 * {@code ((width - pcWidth) / 2, (height - pcHeight) / 2)}.
 */
public record CpsOverlayLayout(
        int pcX,
        int pcY,
        int panelX,
        int panelY,
        float panelScale,
        int toolbarX,
        int toolbarY
) {
    public static final int GAP = 8;
    public static final int MARGIN = 4;
    public static final float MIN_SCALE = 0.55F;

    public int scaled(int nativePx) {
        return Math.max(1, Math.round(nativePx * panelScale));
    }

    public int panelChildX(int nativeOffsetX) {
        return panelX + Math.round(nativeOffsetX * panelScale);
    }

    public int panelChildY(int nativeOffsetY) {
        return panelY + Math.round(nativeOffsetY * panelScale);
    }

    public static CpsOverlayLayout compute(
            int screenWidth,
            int screenHeight,
            int pcWidth,
            int pcHeight,
            int panelWidth,
            int panelHeight,
            int toolbarWidth,
            int toolbarHeight
    ) {
        int pcX = (screenWidth - pcWidth) / 2;
        int pcY = (screenHeight - pcHeight) / 2;
        int toolbarX = pcX + Math.max(0, (pcWidth - toolbarWidth) / 2);
        int toolbarY = Math.min(screenHeight - toolbarHeight - 2, pcY + pcHeight + 4);

        int rightX = pcX + pcWidth + GAP;
        int leftX = pcX - GAP - panelWidth;
        int nativeY = clamp(pcY + (pcHeight - panelHeight) / 2, MARGIN, screenHeight - MARGIN - panelHeight);

        if (fits(rightX, nativeY, panelWidth, panelHeight, screenWidth, screenHeight)) {
            return new CpsOverlayLayout(pcX, pcY, rightX, nativeY, 1F, toolbarX, toolbarY);
        }
        if (fits(leftX, nativeY, panelWidth, panelHeight, screenWidth, screenHeight)) {
            return new CpsOverlayLayout(pcX, pcY, leftX, nativeY, 1F, toolbarX, toolbarY);
        }

        int rightRoom = screenWidth - MARGIN - rightX;
        int leftRoom = pcX - GAP - MARGIN;
        int heightRoom = screenHeight - 2 * MARGIN;
        boolean preferRight = rightRoom >= leftRoom;
        int strip = preferRight ? rightRoom : leftRoom;
        if (strip > 0) {
            float scale = Math.min(1F, Math.min(strip / (float) panelWidth, heightRoom / (float) panelHeight));
            if (scale >= MIN_SCALE) {
                int width = Math.round(panelWidth * scale);
                int height = Math.round(panelHeight * scale);
                int x = preferRight ? rightX : pcX - GAP - width;
                int y = clamp(pcY + (pcHeight - height) / 2, MARGIN, screenHeight - MARGIN - height);
                if (fits(x, y, width, height, screenWidth, screenHeight)) {
                    return new CpsOverlayLayout(pcX, pcY, x, y, scale, toolbarX, toolbarY);
                }
            }
        }

        float scale = Math.min(
                1F,
                Math.min((screenWidth - 2F * MARGIN) / panelWidth, (screenHeight - 2F * MARGIN) / panelHeight)
        );
        int width = Math.max(1, Math.round(panelWidth * scale));
        int height = Math.max(1, Math.round(panelHeight * scale));
        int x = Math.max(MARGIN, screenWidth - MARGIN - width);
        int y = clamp(pcY + (pcHeight - height) / 2, MARGIN, screenHeight - MARGIN - height);
        return new CpsOverlayLayout(pcX, pcY, x, y, scale, toolbarX, toolbarY);
    }

    private static boolean fits(int x, int y, int width, int height, int screenWidth, int screenHeight) {
        return x >= MARGIN
                && y >= MARGIN
                && x + width <= screenWidth - MARGIN
                && y + height <= screenHeight - MARGIN;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }
}
