package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

/** An independently managed sidebar group whose real members can also belong to script folds. */
public interface ExternalFoldGroup {
    List<EmiStack> bkmef$getMembers();

    boolean bkmef$isExpanded();

    interface Member {
        EmiStack bkmef$getRealStack();
    }
}
