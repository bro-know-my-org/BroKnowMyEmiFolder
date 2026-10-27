package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import io.github.broknowmyorg.bkmef.Broknowmyemifolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Opt-in real-client check: runClient -PcompatSmoke, with or without REMI in run/mods. */
@EventBusSubscriber(modid = "broknowmyemifolder", value = Dist.CLIENT)
public final class FoldCompatibilityClientProbe {
    private static boolean done;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) throws Exception {
        Minecraft client = Minecraft.getInstance();
        if (!Boolean.getBoolean("bkmef.compatSmoke") || done || client.getOverlay() != null) return;
        done = true;
        EmiStack white = EmiStack.of(Items.WHITE_WOOL);
        EmiStack red = EmiStack.of(Items.RED_WOOL);
        List<EmiStack> raw = List.of(white, red);
        FoldRegistry.reloadStaticGroups();
        for (String name : List.of("first", "second")) {
            FoldRegistry.add(ResourceLocation.fromNamespaceAndPath("smoke", name), Component.literal(name),
                (FoldMatcher) facts -> facts.id().equals(white.getId()));
        }
        boolean remi = ModList.get().isLoaded("remi");
        EmiIngredient header = null;
        Class<?> manager = null;
        if (remi) {
            Class<?> groupType = Class.forName("com.evandev.remi.feature.stackgroup.data.StackGroup");
            Object group = Class.forName("com.evandev.remi.feature.stackgroup.data.EmiStackGroup")
                .getConstructor(ResourceLocation.class, Set.class, Set.class, List.class, Component.class)
                .newInstance(ResourceLocation.fromNamespaceAndPath("smoke", "remi"), Set.copyOf(raw), Set.of(), List.of(), Component.literal("REMI"));
            Class<?> memberType = Class.forName("com.evandev.remi.feature.stackgroup.GroupedEmiStack");
            List<Object> members = new ArrayList<>();
            for (EmiStack stack : raw) members.add(memberType.getConstructor(EmiStack.class, groupType).newInstance(stack, group));
            header = (EmiIngredient) Class.forName("com.evandev.remi.feature.stackgroup.EmiGroupStack")
                .getConstructor(groupType, List.class).newInstance(group, members);
            expect(header instanceof ExternalFoldGroup, "optional REMI group mixin must apply");
            expect(((ExternalFoldGroup) header).bkmef$getMembers().equals(raw), "member accessor mixin must expose real stacks");
            manager = Class.forName("com.evandev.remi.integration.emi.StackManager");
            setSource(manager, List.of(header));
        } else {
            EmiStackList.filteredStacks = raw;
        }
        EmiScreenManager.ScreenSpace space = new EmiScreenManager.ScreenSpace(0, 0, 4, 2, false,
            List.of(), () -> SidebarType.INDEX, false);
        List<? extends EmiIngredient> stacks = space.getStacks();
        expect(stacks.size() == 3 && stacks.getFirst() instanceof FoldedEmiIngredient,
            "final transformed getStacks must contain both script folders");
        if (remi) expect(stacks.get(2) == header, "REMI's cancellable hook must compose with script folders");
        GuiGraphics graphics = new GuiGraphics(client, client.renderBuffers().bufferSource());
        space.render(EmiDrawContext.wrap(graphics), -1, -1, 0, 0);
        graphics.flush();
        if (remi) {
            header.getClass().getField("isExpanded").setBoolean(header, true);
            setSource(manager, List.of(header, white, red));
            stacks = space.getStacks();
            expect(stacks.size() == 5 && stacks.get(2) == header && stacks.get(3) == white,
                "expanded REMI group must keep its header and real children");
            FoldRegistry.toggle(((FoldedEmiIngredient) stacks.getFirst()).getGroup());
            expect(space.getStacks().getFirst() instanceof ExpandedFoldEmiIngredient,
                "script group expansion must invalidate the composed layout");
            space.render(EmiDrawContext.wrap(graphics), -1, -1, 0, 0);
            graphics.flush();
        }
        // Exercise the final search hook even when an addon supplies raw search results.
        EmiSearch.compiledQuery = new EmiSearch.CompiledQuery("/wool/");
        var searched = EmiScreenManager.class.getDeclaredField("searchedStacks");
        searched.setAccessible(true);
        searched.set(null, raw);
        if (remi) setSource(manager, raw);
        EmiScreenManager.ScreenSpace searchSpace = new EmiScreenManager.ScreenSpace(0, 0, 4, 2, false,
            List.of(), () -> SidebarType.INDEX, true);
        List<? extends EmiIngredient> hits = searchSpace.getStacks();
        expect(hits.size() == 2 && hits.getFirst() instanceof SearchFoldMemberEmiIngredient,
            "raw addon search results must retain script group membership");
        EmiSearch.compiledQuery = new EmiSearch.CompiledQuery("");
        if (remi) setSource(manager, List.of(red));
        else EmiStackList.filteredStacks = List.of(red);
        expect(space.getStacks().equals(List.of(red)), "tab-filtered sources must not reintroduce excluded items");
        if (!remi) {
            // Exercise EMI's actual empty-query fast path, including publication to the search sidebar.
            FoldRegistry.reloadStaticGroups();
            FoldRegistry.add(ResourceLocation.fromNamespaceAndPath("smoke", "layout"), Component.literal("layout"),
                (FoldMatcher) facts -> true);
            List<EmiStack> four = List.of(white, red, EmiStack.of(Items.BLUE_WOOL), EmiStack.of(Items.GREEN_WOOL));
            List<? extends EmiIngredient> folded = FoldLayoutContext.withLayout(new int[] { 4 }, 4,
                () -> FoldRegistry.foldIndex(four));
            Class<?> workerType = Class.forName("dev.emi.emi.search.EmiSearch$SearchWorker");
            var constructor = workerType.getDeclaredConstructor(String.class, List.class);
            constructor.setAccessible(true);
            Runnable worker = (Runnable) constructor.newInstance("", folded);
            var current = EmiSearch.class.getDeclaredField("currentWorker");
            current.setAccessible(true);
            current.set(null, worker);
            worker.run();
            expect(EmiSearch.stacks == folded, "empty-query worker must preserve the selected source for relayout");
            searched.set(null, EmiSearch.stacks);
            var narrow = new EmiScreenManager.ScreenSpace(0, 0, 1, 1, false,
                List.of(), () -> SidebarType.INDEX, true);
            expect(narrow.getStacks().size() == 4, "published empty-query folders must reserve narrow sidebar slots");
        }
        String result = "PASSED: real client, remi=" + remi + ", transformed stack lists and rendering";
        Broknowmyemifolder.LOGGER.info(result);
        Files.writeString(Path.of("bkmef-compat-smoke.txt"), result);
        client.stop();
    }

    private static void setSource(Class<?> manager, List<? extends EmiIngredient> source) throws Exception {
        manager.getField("displayedStacks").set(null, source);
        manager.getField("unsearchedStacks").set(null, source);
        manager.getMethod("invalidateStacks").invoke(null);
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
