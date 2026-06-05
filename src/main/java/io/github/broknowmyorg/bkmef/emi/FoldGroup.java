package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Predicate;

public record FoldGroup(
    ResourceLocation id,
    Component name,
    Predicate<EmiStack> matcher,
    List<Predicate<EmiStack>> unfolders,
    FoldDisplayOptions displayOptions
) {
    public boolean matches(EmiStack stack) {
        return matcher.test(stack);
    }

    public boolean unfolds(EmiStack stack) {
        for (Predicate<EmiStack> unfolder : unfolders) {
            if (unfolder.test(stack)) {
                return true;
            }
        }
        return false;
    }
}
