package io.github.broknowmyorg.bkmef.emi;

import java.util.LinkedHashSet;
import java.util.Set;

public final class FoldLayout {
    private static final int ENTRY_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLOT_PADDING = 1;

    private FoldLayout() {
    }

    public static int reservedSlots(FoldDisplayOptions options, int stackCount, int startLocalOffset, int[] widths, int pageSize) {
        int[] occupiedSlots = occupiedSlots(options, stackCount, startLocalOffset, widths, pageSize);
        if (occupiedSlots.length == 0) {
            return options.reservedSlots(stackCount);
        }

        int lastSlot = 0;
        for (int slot : occupiedSlots) {
            lastSlot = Math.max(lastSlot, slot);
        }
        return Math.max(1, lastSlot + 1);
    }

    public static int[] occupiedSlots(FoldDisplayOptions options, int stackCount, int startLocalOffset, int[] widths, int pageSize) {
        if (stackCount <= 1) {
            return new int[] { 0 };
        }
        if (pageSize <= 0 || widths.length == 0) {
            int reservedSlots = options.reservedSlots(stackCount);
            int[] slots = new int[reservedSlots];
            for (int i = 0; i < reservedSlots; i++) {
                slots[i] = i;
            }
            return slots;
        }

        Set<Integer> occupiedSlots = new LinkedHashSet<>();
        int page = 0;
        int start = Math.floorMod(startLocalOffset, pageSize);
        int row = rowForLocalOffset(start, widths);
        int pixel = (start - rowStartOffset(row, widths)) * ENTRY_SIZE;

        for (int i = 0; i < stackCount; i++) {
            while (pixel + SLOT_PADDING + ICON_SIZE > widths[row] * ENTRY_SIZE) {
                row++;
                pixel = 0;
                if (row >= widths.length) {
                    page++;
                    row = 0;
                }
            }

            int rowStart = rowStartOffset(row, widths);
            int leftLocalOffset = rowStart + (pixel + SLOT_PADDING) / ENTRY_SIZE;
            int rightLocalOffset = rowStart + (pixel + SLOT_PADDING + ICON_SIZE - 1) / ENTRY_SIZE;
            for (int localOffset = leftLocalOffset; localOffset <= rightLocalOffset; localOffset++) {
                occupiedSlots.add(page * pageSize + localOffset - start);
            }
            pixel += options.spread();
        }

        return occupiedSlots.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int rowForLocalOffset(int localOffset, int[] widths) {
        int offset = 0;
        for (int row = 0; row < widths.length; row++) {
            int next = offset + widths[row];
            if (localOffset < next) {
                return row;
            }
            offset = next;
        }
        return Math.max(0, widths.length - 1);
    }

    private static int rowStartOffset(int row, int[] widths) {
        int offset = 0;
        for (int i = 0; i < row; i++) {
            offset += widths[i];
        }
        return offset;
    }
}
