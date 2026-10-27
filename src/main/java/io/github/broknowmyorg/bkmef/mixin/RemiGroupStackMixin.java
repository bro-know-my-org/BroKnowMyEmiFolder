package io.github.broknowmyorg.bkmef.mixin;

import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.emi.ExternalFoldGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Pseudo
@Mixin(targets = "com.evandev.remi.feature.stackgroup.EmiGroupStack", remap = false)
public abstract class RemiGroupStackMixin implements ExternalFoldGroup {
    @Shadow public boolean isExpanded;

    @Shadow public abstract List<?> getItems();

    @Override
    public List<EmiStack> bkmef$getMembers() {
        return getItems().stream().map(item -> ((ExternalFoldGroup.Member) item).bkmef$getRealStack()).toList();
    }

    @Override
    public boolean bkmef$isExpanded() {
        return isExpanded;
    }
}
