package io.github.broknowmyorg.bkmef.kubejs;

import dev.emi.emi.api.stack.EmiStack;
import dev.latvian.mods.kubejs.item.ItemPredicate;
import dev.latvian.mods.kubejs.recipe.viewer.RecipeViewerEntryType;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Wrapper;
import io.github.broknowmyorg.bkmef.Broknowmyemifolder;
import io.github.broknowmyorg.bkmef.emi.FoldDisplayOptions;
import io.github.broknowmyorg.bkmef.emi.FoldRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ClientFoldKubeEvent implements FoldKubeEvent {
    private static final int[] RAINBOW_COLORS = {
        0xFF7A2333,
        0xFF7A3F18,
        0xFF6B5C12,
        0xFF2F6B2D,
        0xFF17665D,
        0xFF244E82,
        0xFF543481,
        0xFF7A2B62
    };

    private int rainbowIndex;

    @Override
    public void fold(Context cx, Object name, Object filter) {
        fold(cx, name, filter, null);
    }

    @Override
    public void fold(Context cx, Object name, Object filter, Object options) {
        Component component = toComponent(name);
        ResourceLocation id = toConfiguredId(cx, options, component);
        add(id, component, itemMatcher(cx, filter), toOptions(cx, options, id));
    }

    @Override
    public void fold(Context cx, Object id, Object name, Object filter, Object options) {
        ResourceLocation groupId = toId(id);
        add(groupId, toComponent(name), itemMatcher(cx, filter), toOptions(cx, options, groupId));
    }

    @Override
    public void foldFluid(Context cx, Object name, Object filter) {
        foldFluid(cx, name, filter, null);
    }

    @Override
    public void foldFluid(Context cx, Object name, Object filter, Object options) {
        Component component = toComponent(name);
        ResourceLocation id = toConfiguredId(cx, options, component);
        add(id, component, fluidMatcher(cx, filter), toOptions(cx, options, id));
    }

    @Override
    public void foldFluid(Context cx, Object id, Object name, Object filter, Object options) {
        ResourceLocation groupId = toId(id);
        add(groupId, toComponent(name), fluidMatcher(cx, filter), toOptions(cx, options, groupId));
    }

    @Override
    public void foldId(Context cx, Object name, Object ids) {
        foldId(cx, name, ids, null);
    }

    @Override
    public void foldId(Context cx, Object name, Object ids, Object options) {
        Component component = toComponent(name);
        ResourceLocation id = toConfiguredId(cx, options, component);
        add(id, component, idMatcher(ids), toOptions(cx, options, id));
    }

    @Override
    public void foldId(Context cx, Object id, Object name, Object ids, Object options) {
        ResourceLocation groupId = toId(id);
        add(groupId, toComponent(name), idMatcher(ids), toOptions(cx, options, groupId));
    }

    @Override
    public void foldMod(Context cx, Object name, Object mods) {
        foldMod(cx, name, mods, null);
    }

    @Override
    public void foldMod(Context cx, Object name, Object mods, Object options) {
        Component component = toComponent(name);
        ResourceLocation id = toConfiguredId(cx, options, component);
        add(id, component, modMatcher(mods), toOptions(cx, options, id));
    }

    @Override
    public void foldMod(Context cx, Object id, Object name, Object mods, Object options) {
        ResourceLocation groupId = toId(id);
        add(groupId, toComponent(name), modMatcher(mods), toOptions(cx, options, groupId));
    }

    @Override
    public void foldSpawnEggs(Context cx, Object name) {
        foldSpawnEggs(cx, name, null);
    }

    @Override
    public void foldSpawnEggs(Context cx, Object name, Object options) {
        Component component = toComponent(name);
        ResourceLocation id = toConfiguredId(cx, options, component);
        add(id, component, spawnEggMatcher(), toOptions(cx, options, id));
    }

    @Override
    public void foldSpawnEggs(Context cx, Object id, Object name, Object options) {
        ResourceLocation groupId = toId(id);
        add(groupId, toComponent(name), spawnEggMatcher(), toOptions(cx, options, groupId));
    }

    @Override
    public void unfold(Context cx, Object groupId, Object filter) {
        FoldRegistry.unfold(toId(groupId), itemMatcher(cx, filter));
    }

    @Override
    public void unfoldFluid(Context cx, Object groupId, Object filter) {
        FoldRegistry.unfold(toId(groupId), fluidMatcher(cx, filter));
    }

    @Override
    public void unfoldId(Context cx, Object groupId, Object ids) {
        FoldRegistry.unfold(toId(groupId), idMatcher(ids));
    }

    @Override
    public void unfoldAll(Context cx, Object filter) {
        FoldRegistry.unfoldAll(itemMatcher(cx, filter));
    }

    @Override
    public void unfoldAllFluid(Context cx, Object filter) {
        FoldRegistry.unfoldAll(fluidMatcher(cx, filter));
    }

    @Override
    public void unfoldAllId(Context cx, Object ids) {
        FoldRegistry.unfoldAll(idMatcher(ids));
    }

    static Predicate<EmiStack> itemMatcher(Context cx, Object filter) {
        ItemPredicate ingredient = (ItemPredicate) RecipeViewerEntryType.ITEM.wrapPredicate(cx, filter);
        ItemStack[] entries = ingredient.kjs$getStackArray();
        return stack -> {
            ItemStack itemStack = stack.getItemStack();
            if (itemStack.isEmpty()) {
                return false;
            }
            for (ItemStack entry : entries) {
                if (ItemStack.isSameItemSameComponents(entry, itemStack)) {
                    return true;
                }
            }
            return ingredient.test(itemStack);
        };
    }

    static Predicate<EmiStack> fluidMatcher(Context cx, Object filter) {
        FluidIngredient ingredient = (FluidIngredient) RecipeViewerEntryType.FLUID.wrapPredicate(cx, filter);
        return stack -> {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            return fluid != null && ingredient.test(new FluidStack(fluid, 1000));
        };
    }

    static Predicate<EmiStack> idMatcher(Object ids) {
        Set<ResourceLocation> idSet = new HashSet<>();
        for (Object id : ListJS.orSelf(ids)) {
            idSet.add(ResourceLocation.parse(String.valueOf(id)));
        }

        return stack -> stack.getId() != null && idSet.contains(stack.getId());
    }

    static Predicate<EmiStack> modMatcher(Object mods) {
        Set<String> namespaces = new HashSet<>();
        for (Object mod : ListJS.orSelf(mods)) {
            namespaces.add(normalizeModId(String.valueOf(mod)));
        }

        return stack -> stack.getId() != null && namespaces.contains(stack.getId().getNamespace().toLowerCase(Locale.ROOT));
    }

    static Predicate<EmiStack> spawnEggMatcher() {
        return stack -> {
            ItemStack itemStack = stack.getItemStack();
            return !itemStack.isEmpty() && itemStack.getItem() instanceof SpawnEggItem;
        };
    }

    static void add(ResourceLocation id, Component component, Predicate<EmiStack> matcher) {
        add(id, component, matcher, FoldDisplayOptions.DEFAULT);
    }

    static void add(ResourceLocation id, Component component, Predicate<EmiStack> matcher, FoldDisplayOptions options) {
        FoldRegistry.add(id, component, matcher, options);
    }

    static Component toComponent(Object name) {
        if (name instanceof Component component) {
            return component;
        }
        String string = String.valueOf(name);
        if (string.startsWith("translate:")) {
            return Component.translatable(string.substring("translate:".length()));
        }
        return Component.literal(string);
    }

    private static ResourceLocation autoId(Component component) {
        return ResourceLocation.fromNamespaceAndPath(Broknowmyemifolder.MODID, sanitizeId(component.getString()));
    }

    private static ResourceLocation toId(Object id) {
        return ResourceLocation.parse(String.valueOf(id));
    }

    private static String normalizeModId(String mod) {
        String string = mod.trim();
        if (string.startsWith("@")) {
            string = string.substring(1);
        }
        return string.toLowerCase(Locale.ROOT);
    }

    private FoldDisplayOptions toOptions(Context cx, Object options, ResourceLocation id) {
        if (options == null) {
            return FoldDisplayOptions.DEFAULT;
        }
        Object unwrapped = Wrapper.unwrapped(options);
        if (unwrapped instanceof Number number) {
            return new FoldDisplayOptions(number.intValue(), FoldDisplayOptions.DEFAULT_FILL_COLOR);
        }

        int spread = FoldDisplayOptions.DEFAULT.spread();
        int fillColor = FoldDisplayOptions.DEFAULT.fillColor();
        Object spreadValue = getOption(cx, unwrapped, "spread");
        Object colorValue = getOption(cx, unwrapped, "color");

        if (spreadValue instanceof Number number) {
            spread = number.intValue();
        }
        if (colorValue != null) {
            fillColor = toColor(colorValue, id);
        }
        return new FoldDisplayOptions(spread, fillColor);
    }

    private static ResourceLocation toConfiguredId(Context cx, Object options, Component component) {
        Object id = getOption(cx, Wrapper.unwrapped(options), "id");
        return id == null ? autoId(component) : toId(id);
    }

    private static Object getOption(Context cx, Object options, String key) {
        if (options == null) {
            return null;
        }
        if (options instanceof Map<?, ?> map) {
            return Wrapper.unwrapped(map.get(key));
        }
        if (options instanceof Scriptable scriptable) {
            Object value = scriptable.get(cx, key, scriptable);
            return value == Scriptable.NOT_FOUND ? null : Wrapper.unwrapped(value);
        }
        return null;
    }

    private int toColor(Object value, ResourceLocation id) {
        Object unwrapped = Wrapper.unwrapped(value);
        if (unwrapped instanceof Number number) {
            return number.intValue();
        }

        String string = String.valueOf(unwrapped).trim();
        if (string.equalsIgnoreCase("rainbow")) {
            int color = RAINBOW_COLORS[Math.floorMod(rainbowIndex, RAINBOW_COLORS.length)];
            rainbowIndex++;
            return color;
        }
        if (string.equalsIgnoreCase("random")) {
            return RAINBOW_COLORS[Math.floorMod(id.hashCode(), RAINBOW_COLORS.length)];
        }
        if (string.startsWith("#")) {
            return Integer.parseUnsignedInt(string.substring(1), 16);
        }
        if (string.startsWith("0x") || string.startsWith("0X")) {
            return Integer.parseUnsignedInt(string.substring(2), 16);
        }
        return Integer.parseUnsignedInt(string, 16);
    }

    private static String sanitizeId(String name) {
        String path = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]+", "_");
        path = path.replaceAll("^_+|_+$", "");
        return path.isEmpty() ? "fold" : path;
    }
}
