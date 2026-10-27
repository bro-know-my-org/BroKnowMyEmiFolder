package io.github.broknowmyorg.bkmef.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.emi.FoldLayoutContext;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import io.github.broknowmyorg.bkmef.emi.FoldedSidebarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EmiScreenManager.ScreenSpace.class)
public class EmiScreenSpaceMixin {
    @Unique
    private List<? extends EmiIngredient> bkmef$renderedStacks;

    @WrapMethod(method = "getStacks", remap = false)
    private List<? extends EmiIngredient> bkmef$getFoldedLayout(Operation<List<? extends EmiIngredient>> original) {
        EmiScreenManager.ScreenSpace space = (EmiScreenManager.ScreenSpace) (Object) this;
        if (space.getType() != SidebarType.INDEX) {
            return original.call();
        }
        if (space.search && EmiSearch.compiledQuery != null && !EmiSearch.compiledQuery.isEmpty()) {
            return FoldRegistry.decorateSearch(original.call());
        }
        // Compose after other addons have selected their source and applied tab filters.
        // The context also rebuilds an empty-search index for this sidebar's row widths.
        return FoldLayoutContext.withLayout(space.widths, space.pageSize,
                () -> FoldRegistry.foldIndex(original.call()));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;begin(III)V"), remap = false)
    private void bkmef$refreshLayoutBatch(StackBatcher batcher, int x, int y, int z, Operation<Void> original) {
        List<? extends EmiIngredient> stacks = ((EmiScreenManager.ScreenSpace) (Object) this).getStacks();
        if (stacks != bkmef$renderedStacks) {
            bkmef$renderedStacks = stacks;
            // Ordinary items are cached separately from our directly drawn folds.
            // Invalidate before begin() so both use the new positions this frame.
            batcher.repopulate();
        }
        original.call(batcher, x, y, z);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;render(Ldev/emi/emi/api/stack/EmiIngredient;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), remap = false)
    private void bkmef$renderFoldedSidebar(StackBatcher batcher, EmiIngredient stack, GuiGraphics draw, int x, int y, float delta, Operation<Void> original) {
        if (!FoldedSidebarRenderer.render(batcher, stack, draw, (EmiScreenManager.ScreenSpace) (Object) this, x, y, delta)) {
            original.call(batcher, stack, draw, x, y, delta);
        }
    }
}
