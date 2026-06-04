package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import io.github.broknowmyorg.bkmef.emi.FoldLayoutContext;
import io.github.broknowmyorg.bkmef.emi.FoldedSidebarRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EmiScreenManager.ScreenSpace.class)
public class EmiScreenSpaceMixin {
    @Inject(method = "getStacks", at = @At("HEAD"), remap = false)
    private void bkmef$beginFoldLayout(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        EmiScreenManager.ScreenSpace space = (EmiScreenManager.ScreenSpace) (Object) this;
        if (space.getType() == SidebarType.INDEX) {
            FoldLayoutContext.begin(space.widths, space.pageSize);
        }
    }

    @Inject(method = "getStacks", at = @At("RETURN"), remap = false)
    private void bkmef$endFoldLayout(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        FoldLayoutContext.end();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/screen/StackBatcher;render(Ldev/emi/emi/api/stack/EmiIngredient;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"), remap = false)
    private void bkmef$renderFoldedSidebar(StackBatcher batcher, EmiIngredient stack, GuiGraphics draw, int x, int y, float delta) {
        if (!FoldedSidebarRenderer.render(batcher, stack, draw, (EmiScreenManager.ScreenSpace) (Object) this, x, y, delta)) {
            batcher.render(stack, draw, x, y, delta);
        }
    }
}
