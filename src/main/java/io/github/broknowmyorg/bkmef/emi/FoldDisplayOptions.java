package io.github.broknowmyorg.bkmef.emi;

public record FoldDisplayOptions(int spread, int fillColor) {
    public static final int MAX_SPREAD = 18 * 16;
    private static final int[] RAINBOW_COLORS = {
        0xFF3F282B, 0xFF403225, 0xFF3B3825, 0xFF293C2D,
        0xFF253B3B, 0xFF293447, 0xFF352D43, 0xFF402D38
    };
    public static final int DEFAULT_FILL_COLOR = 0xFF17324A;
    public static final FoldDisplayOptions DEFAULT = new FoldDisplayOptions(4, DEFAULT_FILL_COLOR);

    public FoldDisplayOptions {
        spread = Math.clamp(spread, 0, MAX_SPREAD);
        fillColor = fillColor | 0xFF000000;
    }

    public static int rainbowColor(int index) {
        return RAINBOW_COLORS[Math.floorMod(index, RAINBOW_COLORS.length)];
    }

    public static int borderColor(int fillColor) {
        int red = Math.min(255, ((fillColor >>> 16) & 255) * 2 + 32);
        int green = Math.min(255, ((fillColor >>> 8) & 255) * 2 + 32);
        int blue = Math.min(255, (fillColor & 255) * 2 + 32);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    public int borderColor() {
        return borderColor(fillColor);
    }

    public int textColor() {
        int border = borderColor();
        int red = (((border >>> 16) & 255) + 255) / 2;
        int green = (((border >>> 8) & 255) + 255) / 2;
        int blue = ((border & 255) + 255) / 2;
        return (red << 16) | (green << 8) | blue;
    }

    public int reservedSlots(int stackCount) {
        if (stackCount <= 1) {
            return 1;
        }
        int width = 18 + (stackCount - 1) * spread;
        return Math.max(1, (width + 17) / 18);
    }
}
