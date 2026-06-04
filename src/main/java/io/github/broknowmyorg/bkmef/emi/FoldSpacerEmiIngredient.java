package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class FoldSpacerEmiIngredient implements EmiIngredient {
    @Override
    public List<EmiStack> getEmiStacks() {
        return List.of(EmiStack.EMPTY);
    }

    @Override
    public EmiIngredient copy() {
        return new FoldSpacerEmiIngredient();
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
        return List.of();
    }
}
