package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EmiSidebars.class)
public class EmiSidebarsMixin {
    @Inject(method = "getStacks", at = @At("RETURN"), cancellable = true, remap = false)
    private static void bkmef$foldIndex(SidebarType type, CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        if (type == SidebarType.INDEX) {
            cir.setReturnValue(FoldRegistry.foldIndex(cir.getReturnValue()));
        }
    }
}
