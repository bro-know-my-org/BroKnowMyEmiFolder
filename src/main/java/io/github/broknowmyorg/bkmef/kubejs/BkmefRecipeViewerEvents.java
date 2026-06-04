package io.github.broknowmyorg.bkmef.kubejs;

import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.plugin.builtin.event.RecipeViewerEvents;

public final class BkmefRecipeViewerEvents {
    public static final EventHandler FOLD = RecipeViewerEvents.GROUP.common("fold", () -> FoldKubeEvent.class);

    private BkmefRecipeViewerEvents() {
    }

    public static EventHandler register() {
        return FOLD;
    }
}
