package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ExpandedFoldEmiIngredient implements EmiIngredient {
    private final FoldGroup group;
    private final EmiIngredient delegate;

    public ExpandedFoldEmiIngredient(FoldGroup group, EmiIngredient delegate) {
        this.group = group;
        this.delegate = delegate;
    }

    public FoldGroup getGroup() {
        return group;
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return delegate.getEmiStacks();
    }

    @Override
    public EmiIngredient copy() {
        return new ExpandedFoldEmiIngredient(group, delegate.copy());
    }

    @Override
    public long getAmount() {
        return delegate.getAmount();
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        delegate.setAmount(amount);
        return this;
    }

    @Override
    public float getChance() {
        return delegate.getChance();
    }

    @Override
    public EmiIngredient setChance(float chance) {
        delegate.setChance(chance);
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        delegate.render(draw, x, y, delta, flags);
        draw.fill(x, y, x + 16, y + 1, 0xE060A5FA);
        draw.fill(x, y + 15, x + 16, y + 16, 0xE060A5FA);
        draw.fill(x, y, x + 1, y + 16, 0xE060A5FA);
        draw.fill(x + 15, y, x + 16, y + 16, 0xE060A5FA);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = new ArrayList<>(delegate.getTooltip());
        tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.alt_click_to_collapse", group.name())
            .withStyle(ChatFormatting.BLUE)));
        return tooltip;
    }
}
