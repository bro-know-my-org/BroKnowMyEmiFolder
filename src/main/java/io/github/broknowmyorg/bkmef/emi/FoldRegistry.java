package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class FoldRegistry {
    private static final List<FoldGroup> GROUPS = new ArrayList<>();
    private static final Set<ResourceLocation> EXPANDED_GROUPS = new HashSet<>();
    private static int version;
    private static List<? extends EmiIngredient> cachedSource;
    private static FoldLayoutContext.Key cachedLayoutKey;
    private static int cachedVersion = -1;
    private static List<? extends EmiIngredient> cachedFolded;

    private FoldRegistry() {
    }

    public static void reloadStaticGroups() {
        GROUPS.clear();
        version++;
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher) {
        add(id, name, matcher, FoldDisplayOptions.DEFAULT);
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher, FoldDisplayOptions displayOptions) {
        GROUPS.removeIf(group -> group.id().equals(id));
        GROUPS.add(new FoldGroup(id, name, matcher, displayOptions));
        version++;
    }

    public static List<? extends EmiIngredient> foldIndex(List<? extends EmiIngredient> source) {
        if (GROUPS.isEmpty() || source.isEmpty()) {
            return source;
        }

        FoldLayoutContext.Key layoutKey = FoldLayoutContext.currentKey();
        if (source == cachedSource && version == cachedVersion && Objects.equals(layoutKey, cachedLayoutKey)) {
            return cachedFolded;
        }

        Map<FoldGroup, List<EmiIngredient>> matchedGroups = new LinkedHashMap<>();
        Map<EmiIngredient, List<FoldGroup>> membership = new IdentityHashMap<>();

        for (EmiIngredient ingredient : source) {
            EmiStack stack = representativeStack(ingredient);
            List<FoldGroup> groups = stack == null ? List.of() : matchingGroups(stack);
            if (!groups.isEmpty()) {
                membership.put(ingredient, groups);
                for (FoldGroup group : groups) {
                    matchedGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(ingredient);
                }
            }
        }

        List<EmiIngredient> folded = new ArrayList<>(source.size());
        Set<FoldGroup> emittedGroups = new HashSet<>();

        for (EmiIngredient ingredient : source) {
            List<FoldGroup> groups = membership.get(ingredient);

            if (groups == null) {
                folded.add(ingredient);
                continue;
            }

            for (FoldGroup group : groups) {
                if (!emittedGroups.add(group)) {
                    continue;
                }

                List<EmiIngredient> groupIngredients = matchedGroups.get(group);
                if (isExpanded(group)) {
                    for (EmiIngredient groupIngredient : groupIngredients) {
                        folded.add(new ExpandedFoldEmiIngredient(group, groupIngredient));
                    }
                } else {
                    List<EmiStack> groupStacks = groupIngredients.stream()
                        .map(FoldRegistry::representativeStack)
                        .toList();
                    int startOffset = folded.size();
                    int reservedSlots = FoldLayoutContext.reservedSlots(group.displayOptions(), groupStacks.size(), startOffset);
                    int spacerSlots = FoldLayoutContext.spacerSlotsBefore(reservedSlots, startOffset);
                    for (int i = 0; i < spacerSlots; i++) {
                        folded.add(new FoldSpacerEmiIngredient());
                    }
                    folded.add(new FoldedEmiIngredient(group, groupStacks));
                    reservedSlots = FoldLayoutContext.reservedSlots(group.displayOptions(), groupStacks.size(), folded.size() - 1);
                    for (int i = 1; i < reservedSlots; i++) {
                        folded.add(new FoldPlaceholderEmiIngredient(group, groupStacks, i));
                    }
                }
            }
        }

        cachedSource = source;
        cachedLayoutKey = layoutKey;
        cachedVersion = version;
        cachedFolded = List.copyOf(folded);
        return cachedFolded;
    }

    public static boolean isExpanded(FoldGroup group) {
        return EXPANDED_GROUPS.contains(group.id());
    }

    public static void toggle(FoldGroup group) {
        if (!EXPANDED_GROUPS.remove(group.id())) {
            EXPANDED_GROUPS.add(group.id());
        }
        version++;
    }

    public static FoldGroup getExpandedGroupFor(EmiIngredient ingredient) {
        EmiStack stack = representativeStack(ingredient);
        if (stack == null) {
            return null;
        }
        for (FoldGroup group : matchingGroups(stack)) {
            if (isExpanded(group)) {
                return group;
            }
        }
        return null;
    }

    private static EmiStack representativeStack(EmiIngredient ingredient) {
        List<EmiStack> stacks = ingredient.getEmiStacks();
        if (stacks.size() != 1) {
            return null;
        }
        EmiStack stack = stacks.getFirst();
        return stack.isEmpty() ? null : stack;
    }

    private static List<FoldGroup> matchingGroups(EmiStack stack) {
        List<FoldGroup> groups = new ArrayList<>();
        for (FoldGroup group : GROUPS) {
            if (group.matches(stack)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }
}
