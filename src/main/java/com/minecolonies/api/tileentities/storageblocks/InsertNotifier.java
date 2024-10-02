package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LISTENER_LOC;

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
        Log.getLogger().info("Got a notifyInsert in InsertNotifier. Sharing event.");
        for (BlockPos pos : this.insertListeners)
        {
            BlockEntity entity = level.getBlockEntity(pos);
            Log.getLogger().info("Sharing with {} which is {}", pos, entity);
            if (entity instanceof IInsertListener listener)
            {
                listener.onInsert(insertPos, itemStack);
            }
        }
    }

    /**
     * Serialize the InsertNotifier to NBT data.
     *
     * @return The NBT data.
     */
    public CompoundTag serializeToNBT()
    {
        final CompoundTag tag = new CompoundTag();

        tag.put(TAG_LISTENER_LOC, insertListeners.stream().map(NbtUtils::writeBlockPos).collect(NBTUtils.toListNBT()));

        return tag;
    }

    /**
     * Populate teh InsertNotifier from NBT
     *
     * @param nbt The NBT data.
     */
    public void read(CompoundTag nbt)
    {
        NBTUtils.streamCompound(nbt.getList(TAG_LISTENER_LOC, Tag.TAG_COMPOUND))
          .map(NbtUtils::readBlockPos)
          .forEach(insertListeners::add);
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
