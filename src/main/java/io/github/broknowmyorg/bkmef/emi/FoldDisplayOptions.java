package io.github.broknowmyorg.bkmef.emi;

public record FoldDisplayOptions(int spread, int fillColor) {
    public static final int DEFAULT_FILL_COLOR = 0xFF17324A;
    public static final FoldDisplayOptions DEFAULT = new FoldDisplayOptions(4, DEFAULT_FILL_COLOR);

    public FoldDisplayOptions {
        spread = Math.max(0, spread);
        fillColor = fillColor | 0xFF000000;
    }

    public int reservedSlots(int stackCount) {
        if (stackCount <= 1) {
            return 1;
        }
        int width = 16 + (stackCount - 1) * spread;
        return Math.max(1, (width + 17) / 18);
    }
}
