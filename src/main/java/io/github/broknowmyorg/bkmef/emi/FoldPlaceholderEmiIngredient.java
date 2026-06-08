package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class FoldPlaceholderEmiIngredient implements FoldSlotEmiIngredient {
    private final FoldGroup group;
    private final List<EmiStack> stacks;
    private final int slotIndex;

    public FoldPlaceholderEmiIngredient(FoldGroup group, List<EmiStack> stacks, int slotIndex) {
        this.group = group;
        this.stacks = stacks;
        this.slotIndex = slotIndex;
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

    public List<EmiStack> getSearchStacks(EmiSearch.CompiledQuery query) {
        return List.of();
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return stacks.isEmpty() ? List.of(EmiStack.EMPTY) : stacks;
    }

    @Override
    public EmiIngredient copy() {
        return new FoldPlaceholderEmiIngredient(group, stacks, slotIndex);
    }

    @Override
    public long getAmount() {
        return 1;
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        return this;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public EmiIngredient setChance(float chance) {
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return new FoldedEmiIngredient(group, stacks).getTooltip();
    }
}
