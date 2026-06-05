package io.github.broknowmyorg.bkmef.kubejs;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.latvian.mods.kubejs.client.KubeSessionData;
import dev.latvian.mods.kubejs.plugin.builtin.event.RecipeViewerEvents;
import dev.latvian.mods.kubejs.recipe.viewer.RecipeViewerEntryType;
import dev.latvian.mods.kubejs.recipe.viewer.server.FluidData;
import dev.latvian.mods.kubejs.recipe.viewer.server.ItemData;
import dev.latvian.mods.kubejs.recipe.viewer.server.RecipeViewerData;
import dev.latvian.mods.kubejs.script.ScriptType;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.function.Predicate;

public final class KubeFoldRegistrar {
    private KubeFoldRegistrar() {
    }

    public static void rebuildFromKube() {
        FoldRegistry.reloadStaticGroups();
        postFoldEvents();
        postGroupEntriesEvents();
        addRemoteData(currentRemoteData());
    }

    public static void rebuildFromKubeAndRefresh() {
        rebuildFromKube();
        EmiScreenManager.repopulatePanels(SidebarType.INDEX);
        EmiSearch.update();
    }

    private static void postFoldEvents() {
        if (BkmefRecipeViewerEvents.FOLD.hasListeners()) {
            BkmefRecipeViewerEvents.FOLD.post(ScriptType.CLIENT, new ClientFoldKubeEvent());
        }
    }

    private static void postGroupEntriesEvents() {
        if (RecipeViewerEvents.GROUP_ENTRIES.hasListeners(RecipeViewerEntryType.ITEM)) {
            RecipeViewerEvents.GROUP_ENTRIES.post(
                ScriptType.CLIENT,
                RecipeViewerEntryType.ITEM,
                new ClientGroupEntriesFoldKubeEvent(RecipeViewerEntryType.ITEM)
            );
        }
        if (RecipeViewerEvents.GROUP_ENTRIES.hasListeners(RecipeViewerEntryType.FLUID)) {
            RecipeViewerEvents.GROUP_ENTRIES.post(
                ScriptType.CLIENT,
                RecipeViewerEntryType.FLUID,
                new ClientGroupEntriesFoldKubeEvent(RecipeViewerEntryType.FLUID)
            );
        }
    }

    private static RecipeViewerData currentRemoteData() {
        KubeSessionData sessionData = KubeSessionData.of(Minecraft.getInstance());
        return sessionData == null ? null : sessionData.recipeViewerData;
    }

    private static void addRemoteData(RecipeViewerData remote) {
        if (remote == null) {
            return;
        }

        for (ItemData.Group group : remote.itemData().groupedEntries()) {
            ClientFoldKubeEvent.add(group.groupId(), group.description(), itemMatcher(group.filter()));
        }
        for (FluidData.Group group : remote.fluidData().groupedEntries()) {
            ClientFoldKubeEvent.add(group.groupId(), group.description(), fluidMatcher(group.filter()));
        }
    }

    private static Predicate<EmiStack> itemMatcher(Ingredient ingredient) {
        return stack -> {
            ItemStack itemStack = stack.getItemStack();
            return !itemStack.isEmpty() && ingredient.test(itemStack);
        };
    }

    private static Predicate<EmiStack> fluidMatcher(FluidIngredient ingredient) {
        return stack -> {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            return fluid != null && ingredient.test(new FluidStack(fluid, 1000));
        };
    }
}
