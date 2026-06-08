package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Predicate;

@FunctionalInterface
public interface FoldMatcher {
    boolean matches(StackFacts facts);

    default boolean test(EmiStack stack) {
        return matches(new StackFacts(stack));
    }

    default Set<ResourceLocation> indexedIds() {
        return Set.of();
    }

    default Set<String> indexedNamespaces() {
        return Set.of();
    }

    static FoldMatcher from(Predicate<EmiStack> predicate) {
        return facts -> predicate.test(facts.stack());
    }
}
