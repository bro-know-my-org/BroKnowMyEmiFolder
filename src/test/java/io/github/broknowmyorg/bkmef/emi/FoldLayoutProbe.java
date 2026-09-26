package io.github.broknowmyorg.bkmef.emi;

import java.util.Arrays;

public final class FoldLayoutProbe {
    private FoldLayoutProbe() {
    }

    public static void main(String[] args) {
        FoldDisplayOptions oddSpread = new FoldDisplayOptions(19, FoldDisplayOptions.DEFAULT_FILL_COLOR);
        // Two 18px cards at x=0 and x=19 need 37px, including the right border.
        expect(3, oddSpread.reservedSlots(2), "fallback reserves the right border");
        expectSlots(new int[] {0, 1, 2}, oddSpread, 2, 0, new int[] {3}, 3);
        // A 36px row cannot contain the second card; it starts on the next row/page.
        expectSlots(new int[] {0, 2}, oddSpread, 2, 0, new int[] {2, 2}, 4);
        expectSlots(new int[] {0, 2}, oddSpread, 2, 0, new int[] {2}, 2);

        FoldDisplayOptions touching = new FoldDisplayOptions(18, FoldDisplayOptions.DEFAULT_FILL_COLOR);
        expectSlots(new int[] {0, 1}, touching, 2, 0, new int[] {2}, 2);
        expectSlots(new int[] {0, 1}, touching, 2, 1, new int[] {2, 2}, 4);
        FoldDisplayOptions overlapping = new FoldDisplayOptions(0, FoldDisplayOptions.DEFAULT_FILL_COLOR);
        expectSlots(new int[] {0}, overlapping, 20, 0, new int[] {2}, 2);
        FoldDisplayOptions excessiveSpread = new FoldDisplayOptions(100_000, FoldDisplayOptions.DEFAULT_FILL_COLOR);
        expect(FoldDisplayOptions.MAX_SPREAD, excessiveSpread.spread(), "script spread limit");
        expectSlots(new int[] {0, 1, 2}, FoldDisplayOptions.DEFAULT, 6, 0, new int[] {2, 2}, 4);
        // Start immediately in the last free slot, then continue the same 4px
        // spread on the next row. No leading spacer or oversized trailing gap.
        expectSlots(new int[] {0, 1, 2}, FoldDisplayOptions.DEFAULT, 6, 4, new int[] {5, 5}, 10);
        checkSidebarLayoutContext(oddSpread);
        System.out.println("Fold layout border checks passed.");
    }

    private static void checkSidebarLayoutContext(FoldDisplayOptions options) {
        // Screenshot regression: four cards starting in column 9 of a 12-column
        // sidebar need six slots when split across rows, not the cached five.
        FoldLayoutContext.Key outside = FoldLayoutContext.currentKey();
        expect(5, FoldLayoutContext.reservedSlots(options, 4, 9), "layout-free search cache");
        int wrappedSlots = FoldLayoutContext.withLayout(new int[] {12, 12}, 24, () -> {
            int slots = FoldLayoutContext.reservedSlots(options, 4, 9);
            return slots;
        });
        expect(6, wrappedSlots, "empty-search sidebar uses its row widths");
        expect(7, FoldLayoutContext.withLayout(new int[] {2, 2}, 4,
                () -> FoldLayoutContext.reservedSlots(options, 4, 0)), "narrow sidebar spans pages");
        if (!outside.equals(FoldLayoutContext.currentKey())) {
            throw new AssertionError("Sidebar layout leaked into subsequent search work");
        }
        try {
            FoldLayoutContext.withLayout(new int[] {12, 12}, 24, () -> {
                throw new IllegalStateException("test cleanup");
            });
        } catch (IllegalStateException expected) {
            if (!outside.equals(FoldLayoutContext.currentKey())) {
                throw new AssertionError("Failed sidebar query leaked its layout");
            }
        }
    }

    private static void expectSlots(int[] expected, FoldDisplayOptions options, int count, int start,
                                    int[] widths, int pageSize) {
        int[] actual = FoldLayout.occupiedSlots(options, count, start, widths, pageSize);
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError("Expected slots " + Arrays.toString(expected) + ", got " + Arrays.toString(actual));
        }
        expect(expected[expected.length - 1] + 1,
                FoldLayout.reservedSlots(options, count, start, widths, pageSize), "reserved slots");
    }

    private static void expect(int expected, int actual, String label) {
        if (actual != expected) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
