package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class FoldCompatibilityProbe {
    public static void main(String[] args) {
        EmiStack shared = new Stack("shared");
        EmiStack other = new Stack("other");
        ForeignGroup foreign = new ForeignGroup(List.of(shared, other));
        FoldRegistry.reloadStaticGroups();
        for (String name : List.of("first", "second")) {
            FoldRegistry.add(id(name), Component.literal(name), (FoldMatcher) facts -> facts.id().equals(shared.getId()));
        }

        List<? extends EmiIngredient> collapsed = FoldRegistry.foldIndex(List.of(foreign));
        expect(collapsed.size() == 3, "two script folders and the foreign header must coexist");
        expect(collapsed.get(2) == foreign, "foreign header identity must survive for its own click handler");
        FoldedEmiIngredient first = (FoldedEmiIngredient) collapsed.get(0);
        FoldedEmiIngredient second = (FoldedEmiIngredient) collapsed.get(1);
        expect(first.getEmiStacks().equals(List.of(shared)), "hidden foreign member must populate first folder");
        expect(second.getEmiStacks().equals(List.of(shared)), "same member must populate second folder");

        foreign.expanded = true;
        List<EmiIngredient> expandedSource = List.of(foreign, shared, other);
        List<? extends EmiIngredient> expanded = FoldRegistry.foldIndex(expandedSource);
        expect(expanded.size() == 5, "expanded foreign children must not be consumed or double-counted");
        expect(expanded.subList(2, 5).equals(expandedSource), "foreign header and children must remain contiguous");
        expect(((FoldedEmiIngredient) expanded.getFirst()).getEmiStacks().size() == 1,
            "a member visible through its foreign group must be counted once per script group");

        FoldRegistry.toggle(first.getGroup());
        List<? extends EmiIngredient> toggled = FoldRegistry.foldIndex(expandedSource);
        expect(toggled.getFirst() instanceof ExpandedFoldEmiIngredient, "first script group must expand");
        expect(toggled.get(1) instanceof FoldedEmiIngredient, "second script group must stay collapsed");
        expect(foreign.expanded && toggled.subList(2, 5).equals(expandedSource), "foreign expansion must stay independent");

        List<? extends EmiIngredient> search = FoldRegistry.decorateSearch(List.of(shared, shared, other));
        expect(search.size() == 2 && search.getFirst() instanceof SearchFoldMemberEmiIngredient,
            "raw addon search results must retain script membership without duplicate hits");
        expect(search.getFirst().getTooltip().size() == 3, "search tooltip must retain both script group memberships");
        expect(FoldRegistry.decorateSearch(List.of(foreign)).getFirst() == foreign,
            "search must preserve the foreign group object");

        // An unconditional matcher must apply to real members, never to the synthetic group header.
        FoldRegistry.reloadStaticGroups();
        FoldRegistry.add(id("all"), Component.literal("all"), (FoldMatcher) facts -> true);
        List<? extends EmiIngredient> all = FoldRegistry.foldIndex(List.of(foreign));
        expect(((FoldedEmiIngredient) all.getFirst()).getEmiStacks().equals(List.of(shared, other)),
            "broad script matchers must not capture foreign headers as real items");
        FoldRegistry.unfoldAll((FoldMatcher) facts -> facts.id().equals(shared.getId()));
        List<? extends EmiIngredient> excluded = FoldRegistry.foldIndex(List.of(foreign));
        expect(((FoldedEmiIngredient) excluded.getFirst()).getEmiStacks().equals(List.of(other)),
            "script exclusions must apply to foreign group members without mutating that group");
        expect(foreign.members.size() == 2, "foreign contents must remain unchanged");

        // Empty-query search results may already be folded for a different sidebar width.
        FoldRegistry.reloadStaticGroups();
        FoldRegistry.add(id("layout"), Component.literal("layout"), (FoldMatcher) facts -> true);
        List<EmiIngredient> raw = List.of(shared, other, new Stack("third"), new Stack("fourth"));
        List<? extends EmiIngredient> wide = FoldLayoutContext.withLayout(new int[] { 4 }, 4, () -> FoldRegistry.foldIndex(raw));
        List<? extends EmiIngredient> narrow = FoldLayoutContext.withLayout(new int[] { 1 }, 1, () -> FoldRegistry.foldIndex(wide));
        expect(narrow.size() == 4, "cached folded source must be relaid out rather than folded again");
        expect(((FoldedEmiIngredient) narrow.getFirst()).getEmiStacks().size() == 4, "relayout must retain all members");

        System.out.println("Fold compatibility checks passed: overlapping groups, independent expansion, search, exclusions, relayout.");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("bkmef_probe", path);
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static class Stack extends EmiStack {
        private final ResourceLocation id;
        private Stack(String name) { id = id(name); }
        @Override public EmiStack copy() { return this; }
        @Override public boolean isEmpty() { return false; }
        @Override public DataComponentPatch getComponentChanges() { return DataComponentPatch.EMPTY; }
        @Override public Object getKey() { return id; }
        @Override public ResourceLocation getId() { return id; }
        @Override public Component getName() { return Component.literal(id.toString()); }
        @Override public List<Component> getTooltipText() { return List.of(getName()); }
        @Override public List<ClientTooltipComponent> getTooltip() { return List.of(); }
        @Override public void render(GuiGraphics draw, int x, int y, float delta, int flags) { }
    }

    private static final class ForeignGroup extends Stack implements ExternalFoldGroup {
        private final List<EmiStack> members;
        private boolean expanded;
        private ForeignGroup(List<EmiStack> members) { super("foreign"); this.members = members; }
        @Override public List<EmiStack> bkmef$getMembers() { return members; }
        @Override public boolean bkmef$isExpanded() { return expanded; }
    }
}
