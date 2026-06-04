package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.emi.ExpandedFoldEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldPlaceholderEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldedEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.SearchFoldMemberEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker")
public class EmiSearchWorkerMixin {
    @Redirect(
        method = "run",
        at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;getEmiStacks()Ljava/util/List;"),
        remap = false
    )
    private List<EmiStack> bkmef$getSearchStacks(EmiIngredient ingredient) {
        if (ingredient instanceof FoldedEmiIngredient folded) {
            return folded.getSearchStacks(EmiSearch.compiledQuery);
        } else if (ingredient instanceof FoldPlaceholderEmiIngredient placeholder) {
            return placeholder.getSearchStacks(EmiSearch.compiledQuery);
        }
        return ingredient.getEmiStacks();
    }

    @Redirect(
        method = "run",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    private boolean bkmef$addSearchStack(List<EmiIngredient> stacks, Object ingredient) {
        if (ingredient instanceof FoldedEmiIngredient folded && EmiSearch.compiledQuery != null && !EmiSearch.compiledQuery.isEmpty()) {
            for (EmiStack stack : folded.getMatchingStacks(EmiSearch.compiledQuery)) {
                SearchFoldMemberEmiIngredient.addOrMerge(stacks, stack, folded.getGroup());
            }
            return true;
        }
        if (ingredient instanceof ExpandedFoldEmiIngredient expanded && EmiSearch.compiledQuery != null && !EmiSearch.compiledQuery.isEmpty()) {
            List<EmiStack> expandedStacks = expanded.getEmiStacks();
            if (expandedStacks.size() == 1) {
                SearchFoldMemberEmiIngredient.addOrMerge(stacks, expandedStacks.getFirst(), expanded.getGroup());
            }
            return true;
        }
        if (ingredient instanceof FoldPlaceholderEmiIngredient) {
            return true;
        }
        return stacks.add((EmiIngredient) ingredient);
    }
}
