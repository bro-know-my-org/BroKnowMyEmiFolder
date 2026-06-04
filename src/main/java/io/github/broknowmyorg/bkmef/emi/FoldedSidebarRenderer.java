package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class FoldedSidebarRenderer {
    private static final int ENTRY_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLOT_PADDING = 1;
    private static final int CARD_RENDER_FLAGS = -1 ^ EmiIngredient.RENDER_AMOUNT;

    private FoldedSidebarRenderer() {
    }

    public static boolean render(StackBatcher batcher, EmiIngredient ingredient, GuiGraphics draw,
                                 EmiScreenManager.ScreenSpace space, int x, int y, float delta) {
        if (!(ingredient instanceof FoldSlotEmiIngredient foldSlot)) {
            return false;
        }

        int pageOffset = getPageOffset(space, x - 1, y - 1);
        if (pageOffset < 0) {
            return true;
        }

        List<EmiStack> stacks = foldSlot.bkmef$getFoldStacks();
        if (stacks.isEmpty()) {
            return true;
        }

        int slotIndex = foldSlot.bkmef$getFoldSlotIndex();
        int primaryPageOffset = Math.floorMod(pageOffset - slotIndex, space.pageSize);
        FoldDisplayOptions options = foldSlot.bkmef$getGroup().displayOptions();
        renderCards(draw, space, primaryPageOffset, slotIndex, stacks, options, delta);
        return true;
    }

    private static void renderCards(GuiGraphics draw, EmiScreenManager.ScreenSpace space, int primaryPageOffset,
                                    int currentSlotIndex, List<EmiStack> stacks, FoldDisplayOptions options, float delta) {
        int page = 0;
        int row = rowForLocalOffset(space, primaryPageOffset);
        int rowStart = rowStartOffset(space, row);
        int pixel = (primaryPageOffset - rowStart) * ENTRY_SIZE;

        for (int i = 0; i < stacks.size(); i++) {
            while (row < space.th && pixel + SLOT_PADDING + ICON_SIZE > space.getWidth(row) * ENTRY_SIZE) {
                row++;
                pixel = 0;
                if (row >= space.th) {
                    page++;
                    row = 0;
                }
            }

            int targetColumn = (pixel + SLOT_PADDING + ICON_SIZE - 1) / ENTRY_SIZE;
            int targetLocalOffset = rowStartOffset(space, row) + targetColumn;
            int targetSlot = page * space.pageSize + targetLocalOffset - primaryPageOffset;
            int inSlotOffset = pixel - targetColumn * ENTRY_SIZE;
            int iconX = space.getX(targetColumn, row) + SLOT_PADDING + inSlotOffset;
            int iconY = space.getY(targetColumn, row) + SLOT_PADDING;

            if (targetSlot == currentSlotIndex) {
                renderCard(stacks.get(i), draw, iconX, iconY, options.fillColor(), delta, i + 1);
            } else if (targetSlot > currentSlotIndex) {
                return;
            }
            pixel += options.spread();
        }
    }

    private static void renderCard(EmiStack stack, GuiGraphics draw, int iconX, int iconY, int fillColor, float delta, int zOffset) {
        draw.pose().pushPose();
        draw.pose().translate(0, 0, zOffset);
        renderCardFill(draw, iconX, iconY, fillColor);
        stack.render(draw, iconX, iconY, delta, CARD_RENDER_FLAGS);
        draw.pose().popPose();
    }

    private static void renderCardFill(GuiGraphics draw, int iconX, int iconY, int fillColor) {
        draw.fill(iconX - SLOT_PADDING, iconY - SLOT_PADDING,
                iconX + ICON_SIZE + SLOT_PADDING, iconY + ICON_SIZE + SLOT_PADDING, fillColor);
    }

    private static int getPageOffset(EmiScreenManager.ScreenSpace space, int slotX, int slotY) {
        int row = (slotY - space.ty) / ENTRY_SIZE;
        if (row < 0 || row >= space.th) {
            return -1;
        }

        int visualColumn = (slotX - space.tx) / ENTRY_SIZE;
        int width = space.getWidth(row);
        int xo = space.rtl ? visualColumn - (space.tw - width) : visualColumn;
        if (xo < 0 || xo >= width) {
            return -1;
        }

        return rowStartOffset(space, row) + xo;
    }

    private static int rowForLocalOffset(EmiScreenManager.ScreenSpace space, int localOffset) {
        int offset = 0;
        for (int row = 0; row < space.th; row++) {
            int next = offset + space.getWidth(row);
            if (localOffset < next) {
                return row;
            }
            offset = next;
        }
        return Math.max(0, space.th - 1);
    }

    private static int rowStartOffset(EmiScreenManager.ScreenSpace space, int row) {
        int offset = 0;
        for (int i = 0; i < row; i++) {
            offset += space.getWidth(i);
        }
        return offset;
    }
}
