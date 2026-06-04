package io.github.broknowmyorg.bkmef.emi;

import java.util.Arrays;

public final class FoldLayoutContext {
    private static final ThreadLocal<Layout> CURRENT = new ThreadLocal<>();

    private FoldLayoutContext() {
    }

    public static void begin(int[] widths, int pageSize) {
        CURRENT.set(new Layout(widths.clone(), pageSize));
    }

    public static void end() {
        CURRENT.remove();
    }

    public static int reservedSlots(FoldDisplayOptions options, int stackCount, int startOffset) {
        Layout layout = CURRENT.get();
        if (layout == null) {
            return options.reservedSlots(stackCount);
        }
        return FoldLayout.reservedSlots(options, stackCount, startOffset, layout.widths(), layout.pageSize());
    }

    public static int spacerSlotsBefore(int reservedSlots, int startOffset) {
        Layout layout = CURRENT.get();
        if (layout == null || reservedSlots <= 1 || layout.pageSize() <= 0 || layout.widths().length == 0) {
            return 0;
        }

        int localOffset = Math.floorMod(startOffset, layout.pageSize());
        int rowStart = 0;
        for (int width : layout.widths()) {
            int rowEnd = rowStart + width;
            if (localOffset < rowEnd) {
                int column = localOffset - rowStart;
                if (column == 0) {
                    return 0;
                }
                int remainingSlots = width - column;
                return reservedSlots > remainingSlots ? remainingSlots : 0;
            }
            rowStart = rowEnd;
        }

        return 0;
    }

    public static Key currentKey() {
        Layout layout = CURRENT.get();
        return layout == null ? Key.EMPTY : new Key(layout.widths(), layout.pageSize());
    }

    private record Layout(int[] widths, int pageSize) {
    }

    public record Key(int[] widths, int pageSize) {
        private static final Key EMPTY = new Key(new int[0], 0);

        public Key {
            widths = widths.clone();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Key key && pageSize == key.pageSize && Arrays.equals(widths, key.widths);
        }

        @Override
        public int hashCode() {
            return 31 * pageSize + Arrays.hashCode(widths);
        }
    }
}
