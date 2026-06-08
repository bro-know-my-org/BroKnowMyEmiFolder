package io.github.broknowmyorg.bkmef.kubejs;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.emi.FoldMatcher;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import io.github.broknowmyorg.bkmef.emi.StackFacts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FoldPerformanceProbe {
    private static final int ENTRIES = Integer.getInteger("bkmef.probe.entries", 20_000);
    private static final int GROUPS = Integer.getInteger("bkmef.probe.groups", 150);
    private static final int WARMUPS = Integer.getInteger("bkmef.probe.warmups", 3);
    private static final int ITERATIONS = Integer.getInteger("bkmef.probe.iterations", 8);

    private FoldPerformanceProbe() {
    }

    public static void main(String[] args) {
        List<EmiIngredient> source = syntheticSource(ENTRIES);
        runScenario("mod+id+pathModulo", source, FoldPerformanceProbe::addCheapDeterministicGroups);
        runScenario("widePredicate", source, FoldPerformanceProbe::addWideGroups);
    }

    private static void runScenario(String name, List<EmiIngredient> source, GroupSetup setup) {
        long[] times = new long[ITERATIONS];
        int foldedSize = 0;

        for (int i = 0; i < WARMUPS; i++) {
            setupGroups(setup);
            FoldRegistry.foldIndex(source);
        }

        for (int i = 0; i < ITERATIONS; i++) {
            setupGroups(setup);
            long start = System.nanoTime();
            foldedSize = FoldRegistry.foldIndex(source).size();
            times[i] = System.nanoTime() - start;
        }

        Arrays.sort(times);
        double min = millis(times[0]);
        double median = millis(times[times.length / 2]);
        double max = millis(times[times.length - 1]);
        double avg = Arrays.stream(times).average().orElse(0.0) / 1_000_000.0;

        System.out.printf(
            "BKMEF probe %-18s entries=%d groups=%d folded=%d min=%.2fms median=%.2fms avg=%.2fms max=%.2fms%n",
            name,
            source.size(),
            GROUPS,
            foldedSize,
            min,
            median,
            avg,
            max
        );
    }

    private static void setupGroups(GroupSetup setup) {
        FoldRegistry.reloadStaticGroups();
        setup.addGroups();
    }

    private static void addCheapDeterministicGroups() {
        for (int group = 0; group < GROUPS; group++) {
            Set<String> namespaces = Set.of("mod_" + group % 40, "mod_" + (group + 7) % 40);
            Set<ResourceLocation> ids = selectedIds(group, 32);
            int modulo = group % 17;
            FoldMatcher matcher = new FoldMatcher() {
                @Override
                public boolean matches(StackFacts facts) {
                    ResourceLocation id = facts.id();
                    return id != null
                        && namespaces.contains(facts.namespace())
                        && !ids.contains(id)
                        && Math.floorMod(id.getPath().hashCode(), 17) == modulo;
                }

                @Override
                public Set<String> indexedNamespaces() {
                    return namespaces;
                }
            };
            FoldRegistry.add(groupId("cheap", group), Component.literal("cheap " + group), matcher);
        }
    }

    private static void addWideGroups() {
        for (int group = 0; group < GROUPS; group++) {
            int namespaceModulo = 2 + group % 9;
            int pathModulo = 3 + group % 11;
            FoldMatcher matcher = new FoldMatcher() {
                @Override
                public boolean matches(StackFacts facts) {
                    ResourceLocation id = facts.id();
                    return id != null
                        && Math.floorMod(facts.namespace().hashCode(), namespaceModulo) == 0
                        && Math.floorMod(id.getPath().hashCode(), pathModulo) == 0;
                }
            };
            FoldRegistry.add(groupId("wide", group), Component.literal("wide " + group), matcher);
        }
    }

    private static List<EmiIngredient> syntheticSource(int size) {
        List<EmiIngredient> source = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String namespace = "mod_" + i % 40;
            String path = "item_" + i;
            source.add(new SyntheticEmiStack(ResourceLocation.fromNamespaceAndPath(namespace, path)));
        }
        return source;
    }

    private static Set<ResourceLocation> selectedIds(int seed, int count) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int item = Math.floorMod(seed * 131 + i * 977, ENTRIES);
            ids.add(ResourceLocation.fromNamespaceAndPath("mod_" + item % 40, "item_" + item));
        }
        return ids;
    }

    private static ResourceLocation groupId(String prefix, int group) {
        return ResourceLocation.fromNamespaceAndPath("bkmef_probe", prefix + "_" + group);
    }

    private static double millis(long nanos) {
        return nanos / 1_000_000.0;
    }

    @FunctionalInterface
    private interface GroupSetup {
        void addGroups();
    }

    private static final class SyntheticEmiStack extends EmiStack {
        private final ResourceLocation id;

        private SyntheticEmiStack(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public EmiStack copy() {
            return this;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public DataComponentPatch getComponentChanges() {
            return DataComponentPatch.EMPTY;
        }

        @Override
        public Object getKey() {
            return id;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public List<Component> getTooltipText() {
            return List.of(getName());
        }

        @Override
        public Component getName() {
            return Component.literal(id.toString());
        }

        @Override
        public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            return List.of();
        }
    }
}
