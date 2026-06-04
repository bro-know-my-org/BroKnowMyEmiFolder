package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

public record FoldGroup(ResourceLocation id, Component name, Predicate<EmiStack> matcher, FoldDisplayOptions displayOptions) {
    public boolean matches(EmiStack stack) {
        return matcher.test(stack);
    }
}
