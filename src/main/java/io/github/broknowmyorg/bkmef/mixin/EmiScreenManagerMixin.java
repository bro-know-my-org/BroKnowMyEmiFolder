package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.emi.ExpandedFoldEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldGroup;
import io.github.broknowmyorg.bkmef.emi.FoldPlaceholderEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import io.github.broknowmyorg.bkmef.emi.FoldedEmiIngredient;
import io.github.broknowmyorg.bkmef.emi.SearchFoldMemberEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private static void bkmef$toggleFold(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        EmiStackInteraction hovered = EmiScreenManager.getHoveredStack((int) mouseX, (int) mouseY, false);
        if (!(hovered instanceof EmiScreenManager.SidebarEmiStackInteraction sidebar) || sidebar.getType() != SidebarType.INDEX) {
            return;
        }

        EmiIngredient ingredient = sidebar.getStack();
        FoldGroup group = null;
        if (ingredient instanceof FoldedEmiIngredient folded) {
            group = folded.getGroup();
        } else if (ingredient instanceof FoldPlaceholderEmiIngredient placeholder) {
            group = placeholder.getGroup();
        } else if (EmiInput.isAltDown()) {
            if (ingredient instanceof SearchFoldMemberEmiIngredient) {
                return;
            } else if (ingredient instanceof ExpandedFoldEmiIngredient expanded) {
                group = expanded.getGroup();
            } else {
                group = FoldRegistry.getExpandedGroupFor(ingredient);
            }
        }

        if (group != null) {
            FoldRegistry.toggle(group);
            EmiScreenManager.repopulatePanels(SidebarType.INDEX);
            EmiSearch.update();
            cir.setReturnValue(true);
        }
    }
}
