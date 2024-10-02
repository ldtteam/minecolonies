package com.minecolonies.api.colony.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class StorageBlockStackInsertEvent extends Event
{
    BlockPos storageBlockPos;

    ResourceKey<Level> dimension;

    ItemStack stack;

    public StorageBlockStackInsertEvent(ResourceKey<Level> dimension, BlockPos pos, ItemStack stack)
    {
        this.storageBlockPos = pos;
        this.dimension = dimension;
        this.stack = stack;
    }

    public BlockPos getPosition()
    {
        return storageBlockPos;
    }

    public ItemStack getInsertedStack()
    {
        return stack;
    }

    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }
}
