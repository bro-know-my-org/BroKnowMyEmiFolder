package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.emi.FoldLayoutContext;
import io.github.broknowmyorg.bkmef.emi.FoldedSidebarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EmiScreenManager.ScreenSpace.class)
public class EmiScreenSpaceMixin {
    @Unique
    private List<? extends EmiIngredient> bkmef$renderedStacks;

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true, remap = false)
    private void bkmef$getFoldedLayout(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        EmiScreenManager.ScreenSpace space = (EmiScreenManager.ScreenSpace) (Object) this;
        if (space.getType() != SidebarType.INDEX) {
            return;
        }
        if (space.search && EmiSearch.compiledQuery != null && !EmiSearch.compiledQuery.isEmpty()) {
            return;
        }
        // The search sidebar also uses searchedStacks for an empty query. That
        // list was folded without row widths and can omit slots after wrapping.
        // Build both empty-search and regular index layouts from the same source.
        cir.setReturnValue(FoldLayoutContext.withLayout(space.widths, space.pageSize,
                () -> EmiSidebars.getStacks(SidebarType.INDEX)));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;begin(III)V"), remap = false)
    private void bkmef$refreshLayoutBatch(StackBatcher batcher, int x, int y, int z) {
        List<? extends EmiIngredient> stacks = ((EmiScreenManager.ScreenSpace) (Object) this).getStacks();
        if (stacks != bkmef$renderedStacks) {
            bkmef$renderedStacks = stacks;
            // Ordinary items are cached separately from our directly drawn folds.
            // Invalidate before begin() so both use the new positions this frame.
            batcher.repopulate();
        }
        batcher.begin(x, y, z);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;render(Ldev/emi/emi/api/stack/EmiIngredient;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), remap = false)
    private void bkmef$renderFoldedSidebar(StackBatcher batcher, EmiIngredient stack, GuiGraphics draw, int x, int y, float delta) {
        if (!FoldedSidebarRenderer.render(batcher, stack, draw, (EmiScreenManager.ScreenSpace) (Object) this, x, y, delta)) {
            batcher.render(stack, draw, x, y, delta);
        }
    }
}
