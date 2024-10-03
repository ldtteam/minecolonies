package com.minecolonies.api.colony.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event used to notify listeners when an inventory
 * is updated.
 */
public class StorageBlockStackInsertEvent extends Event
{
    /**
     * The position of the storage block that was updated.
     */
    final BlockPos storageBlockPos;

    /**
     * The dimension that the storage block is located in.
     */
    final ResourceKey<Level> dimension;

    /**
     * The stack that was inserted.
     */
    final ItemStack stack;

    /**
     * Constructor.
     * 
     * @param dimension The dimension of the updated storage block
     * @param pos       The position of the updated storage block
     * @param stack     The stack that was inserted into the storage block.
     */
    public StorageBlockStackInsertEvent(ResourceKey<Level> dimension, BlockPos pos, ItemStack stack)
    {
        this.storageBlockPos = pos;
        this.dimension = dimension;
        this.stack = stack;
    }

    /**
     * The storage block position.
     * 
     * @return The storage block position
     */
    public BlockPos getPosition()
    {
        return storageBlockPos;
    }

    /**
     * The stack that was inserted into the storage block.
     * 
     * @return The inserted stack.
     */
    public ItemStack getInsertedStack()
    {
        return stack;
    }

    /**
     * The dimension of the storage block.
     * 
     * @return The dimension of the storage block.
     */
    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    @Override
    public String toString()
    {
        return String.format("StorageBlockStackInsertEvent: %s (%d, %d, %d) -> %s", dimension.location(), storageBlockPos.getX(), storageBlockPos.getY(), storageBlockPos.getZ(), stack.getItem().getDescription().getString());
    }
}
