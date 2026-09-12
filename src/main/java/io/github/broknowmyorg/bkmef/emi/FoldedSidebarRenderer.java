package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public final class FoldedSidebarRenderer {
    private static final int ENTRY_SIZE = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLOT_PADDING = 1;
    private static final int CARD_RENDER_FLAGS = -1 ^ EmiIngredient.RENDER_AMOUNT;
    private static final float MIN_CARD_Z_OFFSET = 1.0F;
    private static final float CARD_Z_RANGE = 80.0F;
    private static final float STACK_Z_RANGE = 512.0F;

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
        boolean firstInRow = true;
        int stripLeft = 0;
        int stripFirstSlot = -1;
        List<RenderedCard> cards = new ArrayList<>();

        for (int i = 0; i < stacks.size(); i++) {
            while (row < space.th && pixel + ENTRY_SIZE > space.getWidth(row) * ENTRY_SIZE) {
                row++;
                pixel = 0;
                firstInRow = true;
                if (row >= space.th) {
                    page++;
                    row = 0;
                }
            }

            int targetColumn = (pixel + ENTRY_SIZE - 1) / ENTRY_SIZE;
            int targetLocalOffset = rowStartOffset(space, row) + targetColumn;
            int targetSlot = page * space.pageSize + targetLocalOffset - primaryPageOffset;
            int inSlotOffset = pixel - targetColumn * ENTRY_SIZE;
            int iconX = space.getX(targetColumn, row) + SLOT_PADDING + inSlotOffset;
            int iconY = space.getY(targetColumn, row) + SLOT_PADDING;

            if (firstInRow) {
                stripLeft = iconX - SLOT_PADDING;
                stripFirstSlot = targetSlot;
                firstInRow = false;
            }
            if (targetSlot == currentSlotIndex) {
                cards.add(new RenderedCard(stacks.get(i), iconX, iconY, i, stacks.size()));
            }

            boolean lastInRow = i == stacks.size() - 1
                    || pixel + options.spread() + ENTRY_SIZE > space.getWidth(row) * ENTRY_SIZE;
            if (lastInRow && stripFirstSlot == currentSlotIndex) {
                renderGroupStrip(draw, stripLeft, iconY - SLOT_PADDING,
                        iconX + ICON_SIZE + SLOT_PADDING, options.fillColor());
            }
            // The first slot needs the entire row's bounds before drawing its one
            // shared background. Other slots only need their own item positions.
            if (targetSlot > currentSlotIndex && (stripFirstSlot != currentSlotIndex || lastInRow)) {
                break;
            }
            pixel += options.spread();
        }

        renderCardsInSlot(cards, draw, delta);
    }

    private static void renderCardsInSlot(List<RenderedCard> cards, GuiGraphics draw, float delta) {
        if (cards.isEmpty()) {
            return;
        }

        for (RenderedCard card : cards) {
            float zStep = CARD_Z_RANGE / (card.totalCards() + 1);
            float zOffset = MIN_CARD_Z_OFFSET + (card.totalCards() - card.stackIndex()) * zStep;
            renderCard(card.stack(), draw, card.iconX(), card.iconY(), delta, zOffset, zStep);
        }
    }

    private static void renderCard(EmiStack stack, GuiGraphics draw, int iconX, int iconY,
                                   float delta, float zOffset, float zStep) {
        draw.pose().pushPose();
        try {
            draw.pose().translate(0, 0, zOffset);
            // Item rendering adds 150 Z (decorations can add more). Keep that depth
            // inside this card's layer, above the shared background and below the next card.
            draw.pose().translate(0, 0, zStep * 0.25F);
            // Change only positions: scaling the normal matrix would alter item lighting.
            draw.pose().last().pose().scale(1.0F, 1.0F, zStep * 0.5F / STACK_Z_RANGE);
            stack.render(draw, iconX, iconY, delta, CARD_RENDER_FLAGS);
        } finally {
            draw.pose().popPose();
        }
    }

    private static void renderGroupStrip(GuiGraphics draw, int left, int top, int right, int fillColor) {
        int bottom = top + ENTRY_SIZE;
        int borderColor = FoldDisplayOptions.borderColor(fillColor);
        draw.pose().pushPose();
        try {
            draw.pose().translate(0, 0, MIN_CARD_Z_OFFSET);
            draw.fill(left, top, right, bottom, fillColor);
            // Draw only the row's outer frame, above every item in the group.
            draw.pose().translate(0, 0, CARD_Z_RANGE + 1.0F);
            draw.fill(left, top, right, top + 1, borderColor);
            draw.fill(left, bottom - 1, right, bottom, borderColor);
            draw.fill(left, top, left + 1, bottom, borderColor);
            draw.fill(right - 1, top, right, bottom, borderColor);
        } finally {
            draw.pose().popPose();
        }
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

    private record RenderedCard(EmiStack stack, int iconX, int iconY, int stackIndex, int totalCards) {
    }
}
