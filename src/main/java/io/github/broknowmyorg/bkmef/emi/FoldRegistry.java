package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.BkmefClientConfig;
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
    private static final Map<ResourceLocation, List<Predicate<EmiStack>>> GROUP_UNFOLDERS = new LinkedHashMap<>();
    private static final List<Predicate<EmiStack>> GLOBAL_UNFOLDERS = new ArrayList<>();
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
        GROUP_UNFOLDERS.clear();
        GLOBAL_UNFOLDERS.clear();
        version++;
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher) {
        add(id, name, matcher, FoldDisplayOptions.DEFAULT);
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher, FoldDisplayOptions displayOptions) {
        GROUPS.removeIf(group -> group.id().equals(id));
        GROUPS.add(new FoldGroup(id, name, matcher, unfoldersFor(id), displayOptions));
        version++;
    }

    public static void unfold(ResourceLocation groupId, Predicate<EmiStack> unfolder) {
        unfoldersFor(groupId).add(unfolder);
        version++;
    }

    public static void unfoldAll(Predicate<EmiStack> unfolder) {
        GLOBAL_UNFOLDERS.add(unfolder);
        version++;
    }

    public static int groupCount() {
        return GROUPS.size();
    }

    public static List<? extends EmiIngredient> foldIndex(List<? extends EmiIngredient> source) {
        if (!BkmefClientConfig.isFoldingEnabled() || GROUPS.isEmpty() || source.isEmpty()) {
            return source;
        }

        FoldLayoutContext.Key layoutKey = FoldLayoutContext.currentKey();
        if (source == cachedSource && version == cachedVersion && Objects.equals(layoutKey, cachedLayoutKey)) {
            return cachedFolded;
        }

        FoldMembership foldMembership = collectMembership(source);
        List<EmiIngredient> folded = buildFoldedIndex(source, foldMembership);

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

    private static FoldMembership collectMembership(List<? extends EmiIngredient> source) {
        Map<FoldGroup, List<EmiIngredient>> matchedGroups = new LinkedHashMap<>();
        Map<EmiIngredient, List<FoldGroup>> membership = new IdentityHashMap<>();

        for (EmiIngredient ingredient : source) {
            EmiStack stack = representativeStack(ingredient);
            List<FoldGroup> groups = stack == null ? List.of() : matchingGroups(stack);
            if (groups.isEmpty()) {
                continue;
            }

            membership.put(ingredient, groups);
            for (FoldGroup group : groups) {
                matchedGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(ingredient);
            }
        }

        return new FoldMembership(matchedGroups, membership);
    }

    private static List<EmiIngredient> buildFoldedIndex(List<? extends EmiIngredient> source, FoldMembership foldMembership) {
        List<EmiIngredient> folded = new ArrayList<>(source.size());
        Set<FoldGroup> emittedGroups = new HashSet<>();

        for (EmiIngredient ingredient : source) {
            List<FoldGroup> groups = foldMembership.groupsFor(ingredient);
            if (groups == null) {
                folded.add(ingredient);
                continue;
            }

            for (FoldGroup group : groups) {
                if (emittedGroups.add(group)) {
                    emitGroup(folded, group, foldMembership.ingredientsFor(group));
                }
            }
        }

        return folded;
    }

    private static void emitGroup(List<EmiIngredient> folded, FoldGroup group, List<EmiIngredient> groupIngredients) {
        if (isExpanded(group)) {
            for (EmiIngredient groupIngredient : groupIngredients) {
                folded.add(new ExpandedFoldEmiIngredient(group, groupIngredient));
            }
            return;
        }

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

    private static List<FoldGroup> matchingGroups(EmiStack stack) {
        if (unfoldsGlobally(stack)) {
            return List.of();
        }

        List<FoldGroup> groups = new ArrayList<>();
        for (FoldGroup group : GROUPS) {
            if (group.matches(stack) && !group.unfolds(stack)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }

    private static List<Predicate<EmiStack>> unfoldersFor(ResourceLocation groupId) {
        return GROUP_UNFOLDERS.computeIfAbsent(groupId, ignored -> new ArrayList<>());
    }

    private static boolean unfoldsGlobally(EmiStack stack) {
        for (Predicate<EmiStack> unfolder : GLOBAL_UNFOLDERS) {
            if (unfolder.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private record FoldMembership(Map<FoldGroup, List<EmiIngredient>> matchedGroups,
                                  Map<EmiIngredient, List<FoldGroup>> membership) {
        private List<FoldGroup> groupsFor(EmiIngredient ingredient) {
            return membership.get(ingredient);
        }

        private List<EmiIngredient> ingredientsFor(FoldGroup group) {
            return matchedGroups.getOrDefault(group, List.of());
        }
    }
}
