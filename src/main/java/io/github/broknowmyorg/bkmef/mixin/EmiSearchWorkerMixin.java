package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.emi.ExpandedFoldEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldPlaceholderEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldedEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.SearchFoldMemberEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(targets = "dev.emi.emi.search.EmiSearch$SearchWorker")
public class EmiSearchWorkerMixin {
    @Unique
    private final Map<EmiStack, SearchFoldMemberEmiIngredient> bkmef$members = new HashMap<>();
    @Unique
    private EmiSearch.CompiledQuery bkmef$query;

    @Inject(method = "run", at = @At("HEAD"), remap = false)
    private void bkmef$beginSearch(CallbackInfo ci) {
        bkmef$query = EmiSearch.compiledQuery;
        bkmef$members.clear();
    }

    @Redirect(
        method = "run",
        at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiIngredient;getEmiStacks()Ljava/util/List;"),
        remap = false
    )
    private List<EmiStack> bkmef$getSearchStacks(EmiIngredient ingredient) {
        if (ingredient instanceof FoldedEmiIngredient folded) {
            return folded.getSearchStacks(bkmef$query);
        } else if (ingredient instanceof FoldPlaceholderEmiIngredient placeholder) {
            return placeholder.getSearchStacks(bkmef$query);
        }
        return ingredient.getEmiStacks();
    }

    @Redirect(
        method = "run",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
        remap = false
    )
    private boolean bkmef$addSearchStack(List<EmiIngredient> stacks, Object ingredient) {
        if (ingredient instanceof FoldedEmiIngredient folded && bkmef$query != null && !bkmef$query.isEmpty()) {
            for (EmiStack stack : folded.getMatchingStacks(bkmef$query)) {
                SearchFoldMemberEmiIngredient.addOrMerge(stacks, bkmef$members, stack, folded.getGroup());
            }
            return true;
        }
        if (ingredient instanceof ExpandedFoldEmiIngredient expanded && bkmef$query != null && !bkmef$query.isEmpty()) {
            List<EmiStack> expandedStacks = expanded.getEmiStacks();
            if (expandedStacks.size() == 1) {
                SearchFoldMemberEmiIngredient.addOrMerge(stacks, bkmef$members, expandedStacks.getFirst(), expanded.getGroup());
                return true;
            }
            return stacks.add(expanded);
        }
        if (ingredient instanceof FoldPlaceholderEmiIngredient) {
            return true;
        }
        return stacks.add((EmiIngredient) ingredient);
    }
}
