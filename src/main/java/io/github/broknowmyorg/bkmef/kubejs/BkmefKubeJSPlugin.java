package io.github.broknowmyorg.bkmef.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.event.RecipeViewerEvents;

public class BkmefKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        BkmefRecipeViewerEvents.register();
        registry.register(RecipeViewerEvents.GROUP);
    }
}
