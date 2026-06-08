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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
        Predicate<EmiStack> structured = structuredItemMatcher(cx, filter);
        if (structured != null) {
            return structured;
        }

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

    private static Predicate<EmiStack> structuredItemMatcher(Context cx, Object filter) {
        Object options = Wrapper.unwrapped(filter);
        if (!isStructuredItemFilter(cx, options)) {
            return null;
        }

        ArrayList<Predicate<EmiStack>> matchers = new ArrayList<>();
        addFilterMatcher(cx, options, matchers, "id", ClientFoldKubeEvent::idMatcher);
        addFilterMatcher(cx, options, matchers, "ids", ClientFoldKubeEvent::idMatcher);
        addFilterMatcher(cx, options, matchers, "mod", ClientFoldKubeEvent::modMatcher);
        addFilterMatcher(cx, options, matchers, "mods", ClientFoldKubeEvent::modMatcher);
        addSpawnEggMatcher(cx, options, matchers);
        addFilterMatcher(cx, options, matchers, "block", ClientFoldKubeEvent::blockMatcher);
        addFilterMatcher(cx, options, matchers, "blocks", ClientFoldKubeEvent::blockMatcher);
        addFilterMatcher(cx, options, matchers, "tag", ClientFoldKubeEvent::itemTagMatcher);
        addFilterMatcher(cx, options, matchers, "tags", ClientFoldKubeEvent::itemTagMatcher);
        addFilterMatcher(cx, options, matchers, "blockTag", ClientFoldKubeEvent::blockTagMatcher);
        addFilterMatcher(cx, options, matchers, "blockTags", ClientFoldKubeEvent::blockTagMatcher);
        addFilterMatcher(cx, options, matchers, "all", ClientFoldKubeEvent::allMatcher);
        addFilterMatcher(cx, options, matchers, "any", ClientFoldKubeEvent::anyMatcher);
        addNegatedFilterMatcher(cx, options, matchers, "none");
        addNegatedFilterMatcher(cx, options, matchers, "not");
        addFilterMatcher(cx, options, matchers, "item", ClientFoldKubeEvent::itemMatcher);
        addFilterMatcher(cx, options, matchers, "ingredient", ClientFoldKubeEvent::itemMatcher);

        return stack -> {
            for (Predicate<EmiStack> matcher : matchers) {
                if (!matcher.test(stack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static boolean isStructuredItemFilter(Context cx, Object options) {
        if (options == null) {
            return false;
        }
        return hasOption(cx, options, "all")
            || hasOption(cx, options, "any")
            || hasOption(cx, options, "none")
            || hasOption(cx, options, "not")
            || hasOption(cx, options, "item")
            || hasOption(cx, options, "ingredient")
            || hasOption(cx, options, "id")
            || hasOption(cx, options, "ids")
            || hasOption(cx, options, "mod")
            || hasOption(cx, options, "mods")
            || hasOption(cx, options, "tag")
            || hasOption(cx, options, "tags")
            || hasOption(cx, options, "block")
            || hasOption(cx, options, "blocks")
            || hasOption(cx, options, "blockTag")
            || hasOption(cx, options, "blockTags")
            || hasOption(cx, options, "spawnEggs");
    }

    private static void addFilterMatcher(Context cx, Object options, ArrayList<Predicate<EmiStack>> matchers, String key, ContextMatcherFactory factory) {
        if (hasOption(cx, options, key)) {
            matchers.add(factory.create(cx, getOption(cx, options, key)));
        }
    }

    private static void addFilterMatcher(Context cx, Object options, ArrayList<Predicate<EmiStack>> matchers, String key, MatcherFactory factory) {
        if (hasOption(cx, options, key)) {
            matchers.add(factory.create(getOption(cx, options, key)));
        }
    }

    private static void addSpawnEggMatcher(Context cx, Object options, ArrayList<Predicate<EmiStack>> matchers) {
        if (hasOption(cx, options, "spawnEggs")) {
            Object spawnEggs = getOption(cx, options, "spawnEggs");
            Predicate<EmiStack> matcher = spawnEggMatcher();
            matchers.add(Boolean.FALSE.equals(spawnEggs) ? matcher.negate() : matcher);
        }
    }

    private static void addNegatedFilterMatcher(Context cx, Object options, ArrayList<Predicate<EmiStack>> matchers, String key) {
        if (hasOption(cx, options, key)) {
            matchers.add(anyMatcher(cx, getOption(cx, options, key)).negate());
        }
    }

    private static Predicate<EmiStack> allMatcher(Context cx, Object filters) {
        ArrayList<Predicate<EmiStack>> matchers = itemMatchers(cx, filters);
        return stack -> {
            for (Predicate<EmiStack> matcher : matchers) {
                if (!matcher.test(stack)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Predicate<EmiStack> anyMatcher(Context cx, Object filters) {
        ArrayList<Predicate<EmiStack>> matchers = itemMatchers(cx, filters);
        return stack -> {
            for (Predicate<EmiStack> matcher : matchers) {
                if (matcher.test(stack)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static ArrayList<Predicate<EmiStack>> itemMatchers(Context cx, Object filters) {
        ArrayList<Predicate<EmiStack>> matchers = new ArrayList<>();
        for (Object filter : ListJS.orSelf(filters)) {
            matchers.add(itemMatcher(cx, filter));
        }
        return matchers;
    }

    static Predicate<EmiStack> fluidMatcher(Context cx, Object filter) {
        FluidIngredient ingredient = (FluidIngredient) RecipeViewerEntryType.FLUID.wrapPredicate(cx, filter);
        Map<Fluid, Boolean> cache = new ConcurrentHashMap<>();
        return stack -> {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            return fluid != null && cache.computeIfAbsent(fluid, key -> ingredient.test(new FluidStack(key, 1000)));
        };
    }

    static Predicate<EmiStack> idMatcher(Object ids) {
        Set<ResourceLocation> idSet = new HashSet<>();
        for (Object id : ListJS.orSelf(ids)) {
            idSet.add(ResourceLocation.parse(String.valueOf(id)));
        }

        return stack -> stack.getId() != null && idSet.contains(stack.getId());
    }

    static Predicate<EmiStack> itemTagMatcher(Object tags) {
        Set<TagKey<Item>> tagSet = itemTags(tags);
        return stack -> {
            ItemStack itemStack = stack.getItemStack();
            if (itemStack.isEmpty()) {
                return false;
            }
            for (TagKey<Item> tag : tagSet) {
                if (itemStack.is(tag)) {
                    return true;
                }
            }
            return false;
        };
    }

    static Predicate<EmiStack> modMatcher(Object mods) {
        Set<String> namespaces = new HashSet<>();
        for (Object mod : ListJS.orSelf(mods)) {
            namespaces.add(normalizeModId(String.valueOf(mod)));
        }

        return stack -> stack.getId() != null && namespaces.contains(stack.getId().getNamespace());
    }

    static Predicate<EmiStack> blockMatcher(Object blocks) {
        Set<ResourceLocation> blockSet = new HashSet<>();
        for (Object block : ListJS.orSelf(blocks)) {
            blockSet.add(ResourceLocation.parse(String.valueOf(block)));
        }

        return stack -> {
            Block block = blockOf(stack);
            return block != null && blockSet.contains(BuiltInRegistries.BLOCK.getKey(block));
        };
    }

    static Predicate<EmiStack> blockTagMatcher(Object tags) {
        Set<TagKey<Block>> tagSet = blockTags(tags);
        return stack -> {
            Block block = blockOf(stack);
            if (block == null) {
                return false;
            }
            for (TagKey<Block> tag : tagSet) {
                if (block.builtInRegistryHolder().is(tag)) {
                    return true;
                }
            }
            return false;
        };
    }

    static Predicate<EmiStack> spawnEggMatcher() {
        return stack -> {
            ItemStack itemStack = stack.getItemStack();
            return !itemStack.isEmpty() && itemStack.getItem() instanceof SpawnEggItem;
        };
    }

    private static Block blockOf(EmiStack stack) {
        ItemStack itemStack = stack.getItemStack();
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        return blockItem.getBlock();
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

    private static Set<ResourceLocation> tagIds(Object tags) {
        Set<ResourceLocation> tagSet = new HashSet<>();
        for (Object tag : ListJS.orSelf(tags)) {
            tagSet.add(normalizeTagId(String.valueOf(tag)));
        }
        return tagSet;
    }

    private static Set<TagKey<Item>> itemTags(Object tags) {
        Set<TagKey<Item>> tagSet = new HashSet<>();
        for (ResourceLocation tag : tagIds(tags)) {
            tagSet.add(ItemTags.create(tag));
        }
        return tagSet;
    }

    private static Set<TagKey<Block>> blockTags(Object tags) {
        Set<TagKey<Block>> tagSet = new HashSet<>();
        for (ResourceLocation tag : tagIds(tags)) {
            tagSet.add(BlockTags.create(tag));
        }
        return tagSet;
    }

    private static ResourceLocation normalizeTagId(String tag) {
        String string = tag.trim();
        if (string.startsWith("#")) {
            string = string.substring(1);
        }
        return ResourceLocation.parse(string);
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

    private static boolean hasOption(Context cx, Object options, String key) {
        if (options == null) {
            return false;
        }
        if (options instanceof Map<?, ?> map) {
            return map.containsKey(key);
        }
        if (options instanceof Scriptable scriptable && cx != null) {
            return scriptable.get(cx, key, scriptable) != Scriptable.NOT_FOUND;
        }
        return false;
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

    @FunctionalInterface
    private interface ContextMatcherFactory {
        Predicate<EmiStack> create(Context cx, Object value);
    }

    @FunctionalInterface
    private interface MatcherFactory {
        Predicate<EmiStack> create(Object value);
    }
}
