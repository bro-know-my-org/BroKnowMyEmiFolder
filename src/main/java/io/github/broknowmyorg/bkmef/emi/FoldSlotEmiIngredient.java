package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public interface FoldSlotEmiIngredient extends EmiIngredient {
    FoldGroup bkmef$getGroup();

    List<EmiStack> bkmef$getFoldStacks();

    int bkmef$getFoldSlotIndex();
}
