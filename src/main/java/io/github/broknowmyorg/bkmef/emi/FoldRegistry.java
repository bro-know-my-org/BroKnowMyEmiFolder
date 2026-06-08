package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.BkmefClientConfig;
import io.github.broknowmyorg.bkmef.Broknowmyemifolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class FoldRegistry {
    private static final int LARGE_GROUP_MATCH_THRESHOLD = 512;
    private static final double SLOW_REBUILD_WARN_MS = 100.0;

    private static final List<FoldGroup> GROUPS = new ArrayList<>();
    private static final Map<ResourceLocation, List<FoldMatcher>> GROUP_UNFOLDERS = new LinkedHashMap<>();
    private static final List<FoldMatcher> GLOBAL_UNFOLDERS = new ArrayList<>();
    private static final Set<ResourceLocation> EXPANDED_GROUPS = new HashSet<>();
    private static final Map<ResourceLocation, List<FoldGroup>> GROUPS_BY_ID = new HashMap<>();
    private static final Map<String, List<FoldGroup>> GROUPS_BY_NAMESPACE = new HashMap<>();
    private static final List<FoldGroup> FALLBACK_GROUPS = new ArrayList<>();
    private static final Map<FoldGroup, Integer> GROUP_ORDER = new IdentityHashMap<>();
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
        rebuildGroupIndex();
        version++;
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher) {
        add(id, name, FoldMatcher.from(matcher));
    }

    public static void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher, FoldDisplayOptions displayOptions) {
        add(id, name, FoldMatcher.from(matcher), displayOptions);
    }

    public static void add(ResourceLocation id, Component name, FoldMatcher matcher) {
        add(id, name, matcher, FoldDisplayOptions.DEFAULT);
    }

    public static void add(ResourceLocation id, Component name, FoldMatcher matcher, FoldDisplayOptions displayOptions) {
        GROUPS.removeIf(group -> group.id().equals(id));
        GROUPS.add(new FoldGroup(id, name, matcher, unfoldersFor(id), displayOptions));
        rebuildGroupIndex();
        version++;
    }

    public static void unfold(ResourceLocation groupId, Predicate<EmiStack> unfolder) {
        unfold(groupId, FoldMatcher.from(unfolder));
    }

    public static void unfold(ResourceLocation groupId, FoldMatcher unfolder) {
        unfoldersFor(groupId).add(unfolder);
        version++;
    }

    public static void unfoldAll(Predicate<EmiStack> unfolder) {
        unfoldAll(FoldMatcher.from(unfolder));
    }

    public static void unfoldAll(FoldMatcher unfolder) {
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

        long start = System.nanoTime();
        FoldMembership foldMembership = collectMembership(source);
        List<EmiIngredient> folded = buildFoldedIndex(source, foldMembership);
        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;

        Broknowmyemifolder.LOGGER.debug(
            "Rebuilt folded EMI index: source={}, folded={}, groups={}, matchedEntries={}, memberships={}, time={}ms",
            source.size(),
            folded.size(),
            GROUPS.size(),
            foldMembership.matchedEntryCount(),
            foldMembership.membershipCount(),
            String.format("%.2f", elapsedMs)
        );
        if (elapsedMs >= SLOW_REBUILD_WARN_MS) {
            Broknowmyemifolder.LOGGER.warn(
                "BKMEF folded EMI index rebuild took {}ms for {} source entries and {} groups",
                String.format("%.2f", elapsedMs),
                source.size(),
                GROUPS.size()
            );
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
        for (FoldGroup group : matchingGroups(new StackFacts(stack))) {
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
        int membershipCount = 0;

        for (EmiIngredient ingredient : source) {
            EmiStack stack = representativeStack(ingredient);
            List<FoldGroup> groups = stack == null ? List.of() : matchingGroups(new StackFacts(stack));
            if (groups.isEmpty()) {
                continue;
            }

            membership.put(ingredient, groups);
            membershipCount += groups.size();
            for (FoldGroup group : groups) {
                matchedGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(ingredient);
            }
        }

        logLargeGroups(matchedGroups);

        return new FoldMembership(matchedGroups, membership, membershipCount);
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

    private static List<FoldGroup> matchingGroups(StackFacts facts) {
        if (unfoldsGlobally(facts)) {
            return List.of();
        }

        List<FoldGroup> groups = new ArrayList<>();
        for (FoldGroup group : candidateGroups(facts)) {
            if (group.matches(facts) && !group.unfolds(facts)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }

    private static List<FoldGroup> candidateGroups(StackFacts facts) {
        ResourceLocation id = facts.id();
        List<FoldGroup> byId = id == null ? List.of() : GROUPS_BY_ID.getOrDefault(id, List.of());
        List<FoldGroup> byNamespace = id == null ? List.of() : GROUPS_BY_NAMESPACE.getOrDefault(facts.namespace(), List.of());
        if (byId.isEmpty() && byNamespace.isEmpty()) {
            return FALLBACK_GROUPS;
        }
        if (FALLBACK_GROUPS.isEmpty()) {
            if (byNamespace.isEmpty()) {
                return byId;
            }
            if (byId.isEmpty()) {
                return byNamespace;
            }
        }

        List<FoldGroup> candidates = new ArrayList<>(FALLBACK_GROUPS.size() + byId.size() + byNamespace.size());
        candidates.addAll(FALLBACK_GROUPS);
        candidates.addAll(byId);
        candidates.addAll(byNamespace);
        candidates.sort(Comparator.comparingInt(group -> GROUP_ORDER.getOrDefault(group, Integer.MAX_VALUE)));
        return candidates;
    }

    private static void rebuildGroupIndex() {
        GROUPS_BY_ID.clear();
        GROUPS_BY_NAMESPACE.clear();
        FALLBACK_GROUPS.clear();
        GROUP_ORDER.clear();

        for (int i = 0; i < GROUPS.size(); i++) {
            FoldGroup group = GROUPS.get(i);
            GROUP_ORDER.put(group, i);
            Set<ResourceLocation> ids = group.matcher().indexedIds();
            if (!ids.isEmpty()) {
                for (ResourceLocation id : ids) {
                    GROUPS_BY_ID.computeIfAbsent(id, ignored -> new ArrayList<>()).add(group);
                }
                continue;
            }

            Set<String> namespaces = group.matcher().indexedNamespaces();
            if (!namespaces.isEmpty()) {
                for (String namespace : namespaces) {
                    GROUPS_BY_NAMESPACE.computeIfAbsent(namespace, ignored -> new ArrayList<>()).add(group);
                }
                continue;
            }

            FALLBACK_GROUPS.add(group);
        }
    }

    private static List<FoldMatcher> unfoldersFor(ResourceLocation groupId) {
        return GROUP_UNFOLDERS.computeIfAbsent(groupId, ignored -> new ArrayList<>());
    }

    private static boolean unfoldsGlobally(StackFacts facts) {
        for (FoldMatcher unfolder : GLOBAL_UNFOLDERS) {
            if (unfolder.matches(facts)) {
                return true;
            }
        }
        return false;
    }

    private static void logLargeGroups(Map<FoldGroup, List<EmiIngredient>> matchedGroups) {
        if (!Broknowmyemifolder.LOGGER.isDebugEnabled()) {
            return;
        }

        for (Map.Entry<FoldGroup, List<EmiIngredient>> entry : matchedGroups.entrySet()) {
            int size = entry.getValue().size();
            if (size >= LARGE_GROUP_MATCH_THRESHOLD) {
                Broknowmyemifolder.LOGGER.debug(
                    "BKMEF fold group {} matched {} entries",
                    entry.getKey().id(),
                    size
                );
            }
        }
    }

    private record FoldMembership(Map<FoldGroup, List<EmiIngredient>> matchedGroups,
                                  Map<EmiIngredient, List<FoldGroup>> membership,
                                  int membershipCount) {
        private List<FoldGroup> groupsFor(EmiIngredient ingredient) {
            return membership.get(ingredient);
        }

        private List<EmiIngredient> ingredientsFor(FoldGroup group) {
            return matchedGroups.getOrDefault(group, List.of());
        }

        private int matchedEntryCount() {
            return membership.size();
        }
    }
}
