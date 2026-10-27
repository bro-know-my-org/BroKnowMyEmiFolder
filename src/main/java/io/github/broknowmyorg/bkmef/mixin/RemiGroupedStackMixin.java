package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.emi.ExternalFoldGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "com.evandev.remi.feature.stackgroup.GroupedEmiStack", remap = false)
public abstract class RemiGroupedStackMixin implements ExternalFoldGroup.Member {
    @Shadow @Final public EmiStack realStack;

    @Override
    public EmiStack bkmef$getRealStack() {
        return realStack;
    }
}
