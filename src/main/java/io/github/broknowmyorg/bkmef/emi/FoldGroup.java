package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record FoldGroup(
    ResourceLocation id,
    Component name,
    FoldMatcher matcher,
    List<FoldMatcher> unfolders,
    FoldDisplayOptions displayOptions
) {
    public boolean matches(EmiStack stack) {
        return matches(new StackFacts(stack));
    }

    public boolean matches(StackFacts facts) {
        return matcher.matches(facts);
    }

    public boolean unfolds(EmiStack stack) {
        return unfolds(new StackFacts(stack));
    }

    public boolean unfolds(StackFacts facts) {
        for (FoldMatcher unfolder : unfolders) {
            if (unfolder.matches(facts)) {
                return true;
            }
        }
        return false;
    }
}
