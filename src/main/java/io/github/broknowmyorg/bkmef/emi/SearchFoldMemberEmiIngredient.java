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

public class SearchFoldMemberEmiIngredient implements EmiIngredient {
    private final EmiStack stack;
    private final List<FoldGroup> groups;

    public SearchFoldMemberEmiIngredient(EmiStack stack, FoldGroup group) {
        this(stack, List.of(group));
    }

    private SearchFoldMemberEmiIngredient(EmiStack stack, List<FoldGroup> groups) {
        this.stack = stack;
        this.groups = new ArrayList<>(groups);
    }

    public static void addOrMerge(List<EmiIngredient> results, EmiStack stack, FoldGroup group) {
        for (EmiIngredient result : results) {
            if (result instanceof SearchFoldMemberEmiIngredient member && member.stack.equals(stack)) {
                member.addGroup(group);
                return;
            }
        }
        results.add(new SearchFoldMemberEmiIngredient(stack, group));
    }

    private void addGroup(FoldGroup group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return List.of(stack);
    }

    @Override
    public EmiIngredient copy() {
        return new SearchFoldMemberEmiIngredient(stack.copy(), groups);
    }

    @Override
    public long getAmount() {
        return stack.getAmount();
    }

    @Override
    public EmiIngredient setAmount(long amount) {
        stack.setAmount(amount);
        return this;
    }

    @Override
    public float getChance() {
        return stack.getChance();
    }

    @Override
    public EmiIngredient setChance(float chance) {
        stack.setChance(chance);
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        stack.render(draw, x, y, delta, flags);
        draw.fill(x, y, x + 16, y + 1, 0xE060A5FA);
        draw.fill(x, y + 15, x + 16, y + 16, 0xE060A5FA);
        draw.fill(x, y, x + 1, y + 16, 0xE060A5FA);
        draw.fill(x + 15, y, x + 16, y + 16, 0xE060A5FA);
        if (groups.size() > 1) {
            draw.fill(x + 2, y + 2, x + 14, y + 3, 0xE0FFD166);
            draw.fill(x + 2, y + 13, x + 14, y + 14, 0xE0FFD166);
            draw.fill(x + 2, y + 2, x + 3, y + 14, 0xE0FFD166);
            draw.fill(x + 13, y + 2, x + 14, y + 14, 0xE0FFD166);
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = new ArrayList<>(stack.getTooltip());
        tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.member_of_groups")
            .withStyle(ChatFormatting.BLUE)));
        for (FoldGroup group : groups) {
            tooltip.add(EmiTooltipComponents.of(Component.translatable("tooltip.broknowmyemifolder.preview_entry", group.name())
                .withStyle(ChatFormatting.GRAY)));
        }
        return tooltip;
    }
}
