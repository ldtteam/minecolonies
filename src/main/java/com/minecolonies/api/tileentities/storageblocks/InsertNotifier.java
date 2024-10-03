package com.minecolonies.api.tileentities.storageblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;

/**
 * This defines a notifier that will inform listeners
 * when a stack is inserted to an inventory.
 */
public class InsertNotifier
{
    /**
     * The list of positions of BlockEntities that would like
     * to be notified when items are inserted into this StorageBlock.
     */
    private final Set<BlockPos> insertListeners = new HashSet<>();

    /**
     * Add a new listener to be notified on item inserts.
     *
     * @param pos The position of the listener. Warning: It
     *            must be on the same level as the storage.
     */
    public void addInsertListener(final BlockPos pos)
    {
        this.insertListeners.add(pos);
    }

    /**
     * Notify all the listeners that an item was inserted.
     *
     * @param insertPos The location of the storage where the item was
     *                  inserted.
     * @param itemStack The item stack that was inserted.
     */
    public void notifyInsert(final ResourceKey<Level> dimension, final BlockPos insertPos, final ItemStack itemStack)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Level level = server.getLevel(dimension);
        for (BlockPos pos : this.insertListeners)
        {
            IBuilding building = IColonyManager.getInstance().getBuilding(level, pos);
            building.onInsert(insertPos, itemStack);
        }
    }

    /**
     * The interface that any listener must implement.
     */
    public interface IInsertListener
    {
        /**
         * The function that will be called to notify the listener
         * that a new item stack was inserted.
         *
         * @param insertPos The location of the storage where the item was
         *                  inserted.
         * @param itemStack The item stack that was inserted.
         */
        void onInsert(BlockPos insertPos, ItemStack itemStack);
    }
}
