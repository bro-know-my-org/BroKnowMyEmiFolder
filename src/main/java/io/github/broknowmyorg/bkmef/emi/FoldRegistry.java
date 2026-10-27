package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.broknowmyorg.bkmef.BkmefClientConfig;
import io.github.broknowmyorg.bkmef.Broknowmyemifolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Predicate;

public final class FoldRegistry {
    private static final int LARGE_GROUP_MATCH_THRESHOLD = 512;
    private static final double SLOW_REBUILD_WARN_MS = 100.0;

    private static final Set<ResourceLocation> EXPANDED_GROUPS = new HashSet<>();
    private static final ThreadLocal<RegistryState> STAGING = new ThreadLocal<>();
    private static volatile RegistryState active = new RegistryState();
    private static long rebuildSequence;
    private static long publishedSequence;
    private static int version;
    private static List<? extends EmiIngredient> cachedSource;
    private static int cachedVersion = -1;
    private static FoldMembership cachedMembership;
    private static List<? extends EmiIngredient> cachedSearchSource;
    private static List<? extends EmiIngredient> cachedSearchResult;
    private static int cachedSearchVersion = -1;
    private static final Map<FoldLayoutContext.Key, List<? extends EmiIngredient>> CACHED_LAYOUTS = new LinkedHashMap<>();

    private FoldRegistry() {
    }

    public static void rebuildTogether(Runnable rebuild) {
        if (STAGING.get() != null) {
            throw new IllegalStateException("Nested fold registry rebuild");
        }
        long sequence;
        synchronized (FoldRegistry.class) {
            sequence = ++rebuildSequence;
        }
        RegistryState staged = new RegistryState();
        STAGING.set(staged);
        try {
            rebuild.run();
            synchronized (FoldRegistry.class) {
                if (sequence > publishedSequence) {
                    active = staged;
                    publishedSequence = sequence;
                    version++;
                }
            }
        } finally {
            STAGING.remove();
        }
    }

    public static synchronized void reloadStaticGroups() {
        RegistryState state = state();
        state.groups.clear();
        state.groupUnfolders.clear();
        state.globalUnfolders.clear();
        state.groupIndexDirty = true;
        markChanged();
    }

    public static synchronized void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher) {
        add(id, name, FoldMatcher.from(matcher));
    }

    public static synchronized void add(ResourceLocation id, Component name, Predicate<EmiStack> matcher, FoldDisplayOptions displayOptions) {
        add(id, name, FoldMatcher.from(matcher), displayOptions);
    }

    public static synchronized void add(ResourceLocation id, Component name, FoldMatcher matcher) {
        add(id, name, matcher, FoldDisplayOptions.DEFAULT);
    }

    public static synchronized void add(ResourceLocation id, Component name, FoldMatcher matcher, FoldDisplayOptions displayOptions) {
        RegistryState state = state();
        if (displayOptions == FoldDisplayOptions.DEFAULT) {
            displayOptions = new FoldDisplayOptions(displayOptions.spread(), defaultFillColor(id));
        }
        state.groups.removeIf(group -> group.id().equals(id));
        state.groups.add(new FoldGroup(id, name, matcher, unfoldersFor(state, id), displayOptions));
        state.groupIndexDirty = true;
        markChanged();
    }

    public static synchronized void unfold(ResourceLocation groupId, Predicate<EmiStack> unfolder) {
        unfold(groupId, FoldMatcher.from(unfolder));
    }

    public static synchronized void unfold(ResourceLocation groupId, FoldMatcher unfolder) {
        unfoldersFor(state(), groupId).add(unfolder);
        markChanged();
    }

    public static synchronized void unfoldAll(Predicate<EmiStack> unfolder) {
        unfoldAll(FoldMatcher.from(unfolder));
    }

    public static synchronized void unfoldAll(FoldMatcher unfolder) {
        state().globalUnfolders.add(unfolder);
        markChanged();
    }

    public static synchronized int defaultFillColor(ResourceLocation id) {
        RegistryState state = state();
        for (FoldGroup group : state.groups) {
            if (group.id().equals(id)) {
                return group.displayOptions().fillColor();
            }
        }
        return FoldDisplayOptions.rainbowColor(state.groups.size());
    }

    public static synchronized int groupCount() {
        return state().groups.size();
    }

    public static synchronized List<? extends EmiIngredient> foldIndex(List<? extends EmiIngredient> source) {
        if (source instanceof FoldedIndex index) {
            source = index.source;
        }
        RegistryState state = active;
        if (!BkmefClientConfig.isFoldingEnabled() || state.groups.isEmpty() || source.isEmpty()) {
            return source;
        }

        FoldLayoutContext.Key layoutKey = FoldLayoutContext.currentKey();
        if (source != cachedSource || version != cachedVersion) {
            cachedSource = source;
            cachedVersion = version;
            cachedMembership = null;
            CACHED_LAYOUTS.clear();
        }
        List<? extends EmiIngredient> cachedFolded = CACHED_LAYOUTS.get(layoutKey);
        if (cachedFolded != null) {
            return cachedFolded;
        }

        long start = System.nanoTime();
        FoldMembership foldMembership = cachedMembership;
        if (foldMembership == null) {
            foldMembership = collectMembership(state, source);
            cachedMembership = foldMembership;
        }
        List<EmiIngredient> folded = buildFoldedIndex(source, foldMembership);
        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;

        Broknowmyemifolder.LOGGER.debug(
            "Rebuilt folded EMI index: source={}, folded={}, groups={}, matchedEntries={}, memberships={}, time={}ms",
            source.size(),
            folded.size(),
            state.groups.size(),
            foldMembership.matchedEntryCount(),
            foldMembership.membershipCount(),
            String.format("%.2f", elapsedMs)
        );
        if (elapsedMs >= SLOW_REBUILD_WARN_MS) {
            Broknowmyemifolder.LOGGER.warn(
                "BKMEF folded EMI index rebuild took {}ms for {} source entries and {} groups",
                String.format("%.2f", elapsedMs),
                source.size(),
                state.groups.size()
            );
        }

        cachedFolded = new FoldedIndex(source, List.copyOf(folded));
        if (CACHED_LAYOUTS.size() >= 3) {
            CACHED_LAYOUTS.remove(CACHED_LAYOUTS.keySet().iterator().next());
        }
        CACHED_LAYOUTS.put(layoutKey, cachedFolded);
        return cachedFolded;
    }

    public static synchronized List<? extends EmiIngredient> decorateSearch(List<? extends EmiIngredient> source) {
        if (!BkmefClientConfig.isFoldingEnabled() || active.groups.isEmpty() || source.isEmpty()) {
            return source;
        }
        if (source == cachedSearchSource && version == cachedSearchVersion) {
            return cachedSearchResult;
        }
        List<EmiIngredient> result = new ArrayList<>(source.size());
        Map<EmiStack, SearchFoldMemberEmiIngredient> members = new HashMap<>();
        for (EmiIngredient ingredient : source) {
            EmiStack stack = representativeStack(ingredient);
            List<FoldGroup> groups = stack == null || ingredient instanceof ExternalFoldGroup
                    || ingredient instanceof SearchFoldMemberEmiIngredient
                ? List.of() : matchingGroups(active, new StackFacts(stack));
            if (groups.isEmpty()) {
                result.add(ingredient);
            } else {
                for (FoldGroup group : groups) {
                    SearchFoldMemberEmiIngredient.addOrMerge(result, members, stack, group);
                }
            }
        }
        cachedSearchSource = source;
        cachedSearchVersion = version;
        cachedSearchResult = List.copyOf(result);
        return cachedSearchResult;
    }

    public static synchronized boolean isExpanded(FoldGroup group) {
        return EXPANDED_GROUPS.contains(group.id());
    }

    public static synchronized void toggle(FoldGroup group) {
        if (!EXPANDED_GROUPS.remove(group.id())) {
            EXPANDED_GROUPS.add(group.id());
        }
        version++;
    }

    public static synchronized FoldGroup getExpandedGroupFor(EmiIngredient ingredient) {
        EmiStack stack = representativeStack(ingredient);
        if (stack == null) {
            return null;
        }
        for (FoldGroup group : matchingGroups(active, new StackFacts(stack))) {
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

    private static FoldMembership collectMembership(RegistryState state, List<? extends EmiIngredient> source) {
        Map<FoldGroup, List<EmiIngredient>> matchedGroups = new LinkedHashMap<>();
        Map<EmiIngredient, List<FoldGroup>> membership = new IdentityHashMap<>();
        int membershipCount = 0;
        Set<EmiIngredient> expandedMembers = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EmiIngredient ingredient : source) {
            if (ingredient instanceof ExternalFoldGroup external && external.bkmef$isExpanded()) {
                expandedMembers.addAll(external.bkmef$getMembers());
            }
        }
        Map<FoldGroup, Set<EmiIngredient>> seen = new IdentityHashMap<>();

        for (EmiIngredient ingredient : source) {
            if (expandedMembers.contains(ingredient)) {
                continue;
            }
            List<? extends EmiIngredient> candidates = ingredient instanceof ExternalFoldGroup external
                ? external.bkmef$getMembers() : List.of(ingredient);
            Set<FoldGroup> owners = new LinkedHashSet<>();
            for (EmiIngredient candidate : candidates) {
                EmiStack stack = representativeStack(candidate);
                List<FoldGroup> groups = stack == null ? List.of() : matchingGroups(state, new StackFacts(stack));
                owners.addAll(groups);
                for (FoldGroup group : groups) {
                    if (seen.computeIfAbsent(group, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(candidate)) {
                        matchedGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(candidate);
                        membershipCount++;
                    }
                }
            }
            if (!owners.isEmpty()) {
                membership.put(ingredient, List.copyOf(owners));
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
            // Keep the foreign header and its expanded children together, unchanged.
            if (ingredient instanceof ExternalFoldGroup) {
                folded.add(ingredient);
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
        folded.add(new FoldedEmiIngredient(group, groupStacks));
        for (int i = 1; i < reservedSlots; i++) {
            folded.add(new FoldPlaceholderEmiIngredient(group, groupStacks, i));
        }
    }

    private static List<FoldGroup> matchingGroups(RegistryState state, StackFacts facts) {
        if (unfoldsGlobally(state, facts)) {
            return List.of();
        }

        List<FoldGroup> groups = new ArrayList<>();
        for (FoldGroup group : candidateGroups(state, facts)) {
            if (group.matches(facts) && !group.unfolds(facts)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }

    private static List<FoldGroup> candidateGroups(RegistryState state, StackFacts facts) {
        if (state.groupIndexDirty) {
            rebuildGroupIndex(state);
            state.groupIndexDirty = false;
        }
        ResourceLocation id = facts.id();
        List<FoldGroup> byId = id == null ? List.of() : state.groupsById.getOrDefault(id, List.of());
        List<FoldGroup> byNamespace = id == null ? List.of() : state.groupsByNamespace.getOrDefault(facts.namespace(), List.of());
        if (byId.isEmpty() && byNamespace.isEmpty()) {
            return state.fallbackGroups;
        }
        if (state.fallbackGroups.isEmpty()) {
            if (byNamespace.isEmpty()) {
                return byId;
            }
            if (byId.isEmpty()) {
                return byNamespace;
            }
        }

        List<FoldGroup> candidates = new ArrayList<>(state.fallbackGroups.size() + byId.size() + byNamespace.size());
        candidates.addAll(state.fallbackGroups);
        candidates.addAll(byId);
        candidates.addAll(byNamespace);
        candidates.sort(Comparator.comparingInt(group -> state.groupOrder.getOrDefault(group, Integer.MAX_VALUE)));
        return candidates;
    }

    private static void rebuildGroupIndex(RegistryState state) {
        state.groupsById.clear();
        state.groupsByNamespace.clear();
        state.fallbackGroups.clear();
        state.groupOrder.clear();

        for (int i = 0; i < state.groups.size(); i++) {
            FoldGroup group = state.groups.get(i);
            state.groupOrder.put(group, i);
            Set<ResourceLocation> ids = group.matcher().indexedIds();
            if (!ids.isEmpty()) {
                for (ResourceLocation id : ids) {
                    state.groupsById.computeIfAbsent(id, ignored -> new ArrayList<>()).add(group);
                }
                continue;
            }

            Set<String> namespaces = group.matcher().indexedNamespaces();
            if (!namespaces.isEmpty()) {
                for (String namespace : namespaces) {
                    state.groupsByNamespace.computeIfAbsent(namespace, ignored -> new ArrayList<>()).add(group);
                }
                continue;
            }

            state.fallbackGroups.add(group);
        }
    }

    private static List<FoldMatcher> unfoldersFor(RegistryState state, ResourceLocation groupId) {
        return state.groupUnfolders.computeIfAbsent(groupId, ignored -> new ArrayList<>());
    }

    private static boolean unfoldsGlobally(RegistryState state, StackFacts facts) {
        for (FoldMatcher unfolder : state.globalUnfolders) {
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

    private static RegistryState state() {
        RegistryState staged = STAGING.get();
        return staged == null ? active : staged;
    }

    private static void markChanged() {
        if (STAGING.get() == null) {
            version++;
        }
    }

    private static final class RegistryState {
        private final List<FoldGroup> groups = new ArrayList<>();
        private final Map<ResourceLocation, List<FoldMatcher>> groupUnfolders = new LinkedHashMap<>();
        private final List<FoldMatcher> globalUnfolders = new ArrayList<>();
        private final Map<ResourceLocation, List<FoldGroup>> groupsById = new HashMap<>();
        private final Map<String, List<FoldGroup>> groupsByNamespace = new HashMap<>();
        private final List<FoldGroup> fallbackGroups = new ArrayList<>();
        private final Map<FoldGroup, Integer> groupOrder = new IdentityHashMap<>();
        private boolean groupIndexDirty = true;
    }

    /** Retain the raw source so a cached/empty-search result can be laid out for another sidebar. */
    private static final class FoldedIndex extends AbstractList<EmiIngredient> implements RandomAccess {
        private final List<? extends EmiIngredient> source;
        private final List<EmiIngredient> entries;

        private FoldedIndex(List<? extends EmiIngredient> source, List<EmiIngredient> entries) {
            this.source = source;
            this.entries = entries;
        }

        @Override public EmiIngredient get(int index) { return entries.get(index); }
        @Override public int size() { return entries.size(); }
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
