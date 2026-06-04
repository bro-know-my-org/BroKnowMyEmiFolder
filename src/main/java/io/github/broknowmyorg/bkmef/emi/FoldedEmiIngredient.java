package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FoldedEmiIngredient implements FoldSlotEmiIngredient {
    private final FoldGroup group;
    private final List<EmiStack> stacks;
    private final int slotIndex;
    private long amount;
    private float chance;
    private EmiSearch.CompiledQuery cachedQuery;
    private List<EmiStack> cachedMatches;

    public FoldedEmiIngredient(FoldGroup group, List<EmiStack> stacks) {
        this(group, stacks, 0, 1, 1);
    }

    private FoldedEmiIngredient(FoldGroup group, List<EmiStack> stacks, int slotIndex, long amount, float chance) {
        this.group = group;
        this.stacks = List.copyOf(stacks);
        this.slotIndex = slotIndex;
        this.amount = amount;
        this.chance = chance;
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return stacks.isEmpty() ? List.of(EmiStack.EMPTY) : stacks;
    }

    public List<EmiStack> getSearchStacks(EmiSearch.CompiledQuery query) {
        List<EmiStack> matchingStacks = getMatchingStacks(query);
        return matchingStacks.isEmpty() ? List.of() : List.of(matchingStacks.getFirst());
    }

    public List<EmiStack> getMatchingStacks(EmiSearch.CompiledQuery query) {
        if (query == null || query.isEmpty()) {
            return getEmiStacks();
        }
        if (query == cachedQuery && cachedMatches != null) {
            return cachedMatches;
        }
        List<EmiStack> matchingStacks = new ArrayList<>();
        for (EmiStack stack : stacks) {
            if (query.test(stack)) {
                matchingStacks.add(stack);
            }
        }
        cachedQuery = query;
        cachedMatches = List.copyOf(matchingStacks);
        return cachedMatches;
    }

    public FoldGroup getGroup() {
        return group;
    }

    @Override
    public FoldGroup bkmef$getGroup() {
        return group;
    }

    @Override
    public List<EmiStack> bkmef$getFoldStacks() {
        return stacks;
    }

    @Override
    public int bkmef$getFoldSlotIndex() {
        return slotIndex;
    }

    @Override
    public EmiIngredient copy() {
        return new FoldedEmiIngredient(group, List.copyOf(stacks), slotIndex, amount, chance);
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        if (!stacks.isEmpty()) {
            stacks.getFirst().render(draw, x, y, delta, flags);
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(EmiTooltipComponents.of(group.name().copy().withStyle(ChatFormatting.GOLD)));
        tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.folded_entries", stacks.size()).withStyle(ChatFormatting.GRAY)));
        tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.click_to_expand").withStyle(ChatFormatting.BLUE)));

        int previewSize = Math.min(stacks.size(), 6);
        for (int i = 0; i < previewSize; i++) {
            tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.preview_entry", stacks.get(i).getName())
                .withStyle(ChatFormatting.DARK_GRAY)));
        }
        if (stacks.size() > previewSize) {
            tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.more_entries")
                .withStyle(ChatFormatting.DARK_GRAY)));
        }

        return tooltip;
    }
}
