package io.github.broknowmyorg.bkmef.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

public final class StackFacts {
    private final EmiStack stack;
    private final ResourceLocation id;
    private ItemStack itemStack;
    private boolean itemStackResolved;
    private Block block;
    private boolean blockResolved;

    public StackFacts(EmiStack stack) {
        this.stack = stack;
        this.id = stack.getId();
    }

    public EmiStack stack() {
        return stack;
    }

    public ResourceLocation id() {
        return id;
    }

    public String namespace() {
        return id == null ? "" : id.getNamespace();
    }

    public ItemStack itemStack() {
        if (!itemStackResolved) {
            itemStack = stack.getItemStack();
            itemStackResolved = true;
        }
        return itemStack;
    }

    public Block block() {
        if (!blockResolved) {
            ItemStack itemStack = itemStack();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem) {
                block = blockItem.getBlock();
            }
            blockResolved = true;
        }
        return block;
    }

    public boolean itemIn(TagKey<Item> tag) {
        ItemStack itemStack = itemStack();
        return !itemStack.isEmpty() && itemStack.is(tag);
    }

    public boolean blockIn(TagKey<Block> tag) {
        Block block = block();
        return block != null && block.builtInRegistryHolder().is(tag);
    }

    public boolean blockIs(ResourceLocation blockId) {
        Block block = block();
        return block != null && blockId.equals(BuiltInRegistries.BLOCK.getKey(block));
    }

    public boolean spawnEgg() {
        ItemStack itemStack = itemStack();
        return !itemStack.isEmpty() && itemStack.getItem() instanceof SpawnEggItem;
    }
}
