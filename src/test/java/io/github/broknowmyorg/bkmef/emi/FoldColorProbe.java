package io.github.broknowmyorg.bkmef.emi;

import io.github.broknowmyorg.bkmef.kubejs.ClientFoldKubeEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class FoldColorProbe {
    public static void main(String[] args) {
        FoldRegistry.reloadStaticGroups();
        ResourceLocation first = ResourceLocation.parse("probe:first");
        ResourceLocation second = ResourceLocation.parse("probe:second");
        ResourceLocation third = ResourceLocation.parse("probe:third");
        FoldMatcher matcher = facts -> false;
        FoldRegistry.add(first, Component.literal("First"), matcher);
        FoldRegistry.add(second, Component.literal("Second"), matcher);
        int original = FoldRegistry.defaultFillColor(first);
        FoldRegistry.add(first, Component.literal("Replacement"), matcher);
        expect(original, FoldRegistry.defaultFillColor(first), "default replacement retains color");
        expect(FoldDisplayOptions.rainbowColor(1), FoldRegistry.defaultFillColor(second), "other group retains color");

        ClientFoldKubeEvent event = new ClientFoldKubeEvent();
        for (Object options : List.of(8, Map.of("spread", 8), Map.of())) {
            event.foldId(null, first.toString(), "Replacement", "minecraft:stone", options);
            expect(original, FoldRegistry.defaultFillColor(first), "script replacement retains color: " + options);
        }
        event.foldId(null, first.toString(), "Custom", "minecraft:stone", Map.of("color", "#123456"));
        expect(0xFF123456, FoldRegistry.defaultFillColor(first), "explicit color overrides default");
        event.foldId(null, first.toString(), "Custom replacement", "minecraft:stone", null);
        expect(0xFF123456, FoldRegistry.defaultFillColor(first), "omitted color retains explicit color");
        event.foldId(null, third.toString(), "Third", "minecraft:stone", Map.of("spread", 8));
        expect(FoldDisplayOptions.rainbowColor(2), FoldRegistry.defaultFillColor(third), "replacement does not consume next color");
        expect(3, FoldRegistry.groupCount(), "replacements do not add groups");
        FoldRegistry.reloadStaticGroups();
        System.out.println("Fold default color checks passed.");
    }

    private static void expect(int expected, int actual, String label) {
        if (actual != expected) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
