package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.event.StorageBlockStackInsertEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The manager for letting blocks listen for when
 * storage blocks are updated or items are inserted into them.
 */
public interface IStorageBlockNotificationManager
{
    /**
     * Get the singleton instance of the manager.
     * 
     * @return The singleton instance
     */
    public static IStorageBlockNotificationManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getStorageBlockNotificationManager();
    }

    /**
     * Register a new listener that wants to hear updates for a storage block
     * at a specific position.
     * 
     * @param dimension The dimension of the storage block
     * @param targetPos The position of the storage block
     * @param listenerPos The position of the listener
     */
    void addListener(final ResourceKey<Level> dimension, final BlockPos targetPos, final BlockPos listenerPos);

    /**
     * Called whenever a storage block receives new items.
     * 
     * @param event the data about the insert
     */
    void onInsert(StorageBlockStackInsertEvent event);
}
