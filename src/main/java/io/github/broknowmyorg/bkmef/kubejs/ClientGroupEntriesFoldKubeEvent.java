package io.github.broknowmyorg.bkmef.kubejs;

import dev.latvian.mods.kubejs.recipe.viewer.GroupEntriesKubeEvent;
import dev.latvian.mods.kubejs.recipe.viewer.RecipeViewerEntryType;
import dev.latvian.mods.rhino.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientGroupEntriesFoldKubeEvent implements GroupEntriesKubeEvent {
    private final RecipeViewerEntryType type;

    public ClientGroupEntriesFoldKubeEvent(RecipeViewerEntryType type) {
        this.type = type;
    }

    @Override
    public void group(Context cx, Object filter, ResourceLocation groupId, Component description) {
        if (type == RecipeViewerEntryType.ITEM) {
            ClientFoldKubeEvent.add(groupId, description, ClientFoldKubeEvent.itemMatcher(cx, filter));
        } else if (type == RecipeViewerEntryType.FLUID) {
            ClientFoldKubeEvent.add(groupId, description, ClientFoldKubeEvent.fluidMatcher(cx, filter));
        }
    }
}
