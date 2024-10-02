package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.event.StorageBlockStackInsertEvent;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.tileentities.storageblocks.RackStorageBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The abstract StorageBlock class that all implementations should
 * inherit from.
 */
public abstract class AbstractStorageBlock
{
    /**
     * The position of the block entity in the world.
     */
    protected BlockPos targetPos;

    /**
     * The dimension that the block entity is located in.
     */
    protected ResourceKey<Level> dimension;

    /**
     * The level that the storage block has been upgraded to.
     */
    protected int upgradeLevel = 0;

    /**
     * Constructor
     *
     * @param targetPos The location of the block
     * @param level     The world the block is in
     */
    public AbstractStorageBlock(final BlockPos targetPos, ResourceKey<Level> dimension)
    {
        this.targetPos = targetPos;
        this.dimension = dimension;
    }

    /**
     * Gets the current upgrade level of the storageblock
     *
     * @return The current level
     */
    public int getUpgradeLevel()
    {
        return upgradeLevel;
    }

    /**
     * Upgrades the size of the storage, if applicable.
     */
    public void increaseUpgradeLevel()
    {
        ++upgradeLevel;
    }

    /**
     * The position of the target storage block.
     *
     * @return The position.
     */
    public final BlockPos getPosition()
    {
        return targetPos;
    }

    public final ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    /**
     * Whether the block is currently loaded.
     *
     * @return Whether it's loaded.
     */
    public final boolean isLoaded()
    {

        return WorldUtil.isBlockLoaded(getLevel(), targetPos);
    }

    /**
     * Write this object to NBT.
     *
     * @return The NBT data for this object.
     */
    public CompoundTag serializeNBT()
    {
        CompoundTag result = new CompoundTag();

        BlockPosUtil.write(result, TAG_POS, targetPos);
        result.putString(TAG_DIMENSION, dimension.location().toString());
        result.putString(TAG_STORAGE_BLOCK_TYPE, getStorageBlockClass().getName());

        return result;
    }

    /**
     * Load block data from NBT data.
     *
     * @param nbt The NBT data for the block
     */
    public static AbstractStorageBlock fromNBT(final CompoundTag nbt)
    {
        BlockPos targetPos = BlockPosUtil.read(nbt, TAG_POS);
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString(TAG_DIMENSION)));
        String className = nbt.getString(TAG_STORAGE_BLOCK_TYPE);

        Class<? extends AbstractStorageBlock> clazz = RackStorageBlock.class;
        try {
            clazz = Class.forName(className).asSubclass(AbstractStorageBlock.class);
        } catch (ClassNotFoundException e) {
            Log.getLogger().error("Could not find class {}, defaulting to AbstractStorageBlock", className);
        }

        try {
            Constructor<? extends AbstractStorageBlock> constructor = clazz.getDeclaredConstructor(BlockPos.class, ResourceKey.class);
            Log.getLogger().info(constructor);
            return constructor.newInstance(targetPos, dimension);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            Log.getLogger().error("Failed to initialize StorageBlock at {}: {}", targetPos, e);

            if (e instanceof InvocationTargetException invTargetException)
            {
                Log.getLogger().error("Underlying exception: {}", invTargetException.getTargetException());
                invTargetException.getTargetException().printStackTrace();
            }
            // for (StackTraceElement line : e.getStackTrace())
            // {
            //     Log.getLogger().info(line.toString());
            // }
        }

        return null;
    }

    /**
     * Adds the full item stack to the first available slot in
     * the storage block.
     *
     * @param stack The ItemStack to insert
     * @return if the full transfer was successful
     */
    public boolean insertFullStack(final ItemStack stack)
    {
        final boolean result = insertFullStackImpl(stack);

        if (result)
        {
            MinecraftForge.EVENT_BUS.post(new StorageBlockStackInsertEvent(dimension, targetPos, stack));
        }

        return result;
    }

    /**
     * Get the level from the storageblock's dimension
     * 
     * @return The level this storage block is in
     */
    protected Level getLevel()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.levels.get(dimension);
    }

    /**
     * Whether the storage block will notify on item inserts or
     * whether we need to rely on block updates.
     * 
     * @return if the storage block notifies on item inserts
     */
    public abstract boolean supportsItemInsertNotification();

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param storage The item to check for
     */
    public abstract int getItemCount(final ItemStorage storage);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param predicate The predicate used to select items
     */
    public abstract int getItemCount(final Predicate<ItemStack> predicate);

    /**
     * Gets the matching count for a specific
     * item stack and can ignore NBT and damage as well.
     *
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    public abstract int getItemCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Check whether the position is still valid for this storage interface
     *
     * @param building The building the block is in
     * @return Whether the position is still valid
     */
    public abstract boolean isStillValid(final IBuilding building);

    /**
     * Check whether the position is still valid for this storage interface
     *
     * @param building A view of the building the block is in
     * @return Whether the position is still valid
     */
    public abstract boolean isStillValid(final IBuildingView building);

    /**
     * Return the number of free slots in the container.
     *
     * @return The free slots
     */
    public abstract int getFreeSlots();

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    public abstract boolean isEmpty();

    /**
     * Whether the storage block has 0 completely free slots.
     *
     * @return True if there are no free slots, false otherwise.
     */
    public abstract boolean isFull();

    /**
     * Return whether the storageblock contains a matching item stack
     *
     * @param stack        The item type to compare
     * @param count        The amount that must be present
     * @param ignoreDamage Whether the items should have matching damage values
     * @return Whether the storageblock contains the match
     */
    public abstract boolean hasItemStack(final ItemStack stack, final int count, final boolean ignoreDamage);

    /**
     * Return whether the storageblock contains any items matching the predicate
     *
     * @param predicate The predicate to check against
     * @return Whether the storageblock has any matches
     */
    public abstract boolean hasItemStack(final Predicate<ItemStack> predicate);

    /**
     * Get any matching item stacks within the storage block.
     *
     * @param predicate The predicate to test against
     * @return The list of matching item stacks
     */
    public abstract List<ItemStack> getMatching(@NotNull final Predicate<ItemStack> predicate);

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    public abstract Map<ItemStorage, Integer> getAllContent();

    /**
     * Removes an item stack matching the given predicate from the storage block
     * and returns it.
     *
     * @param predicate The predicate to match
     * @param simulate If true, actually remove the item.
     * @return The matching item stack, or ItemStack.EMPTY
     */
    public abstract ItemStack extractItem(final Predicate<ItemStack> predicate, boolean simulate);

    /**
     * Removes an item stack matching the given predicate from the storage block
     * and returns it.
     *
     * @param itemStack The item stack to remove
     * @param count The amount to remove
     * @param simulate If true, actually remove the item.
     * @return The matching item stack, or ItemStack.EMPTY
     */
    public abstract ItemStack extractItem(final ItemStack itemStack, int count, boolean simulate);

    /**
     * Finds the first ItemStack that matches the given predicate and returns it. Return
     * null if it doesn't exist.
     * 
     * @param predicate The predicate to test against
     * @return The matching stack or else null
     */
    public abstract ItemStack findFirstMatch(final Predicate<ItemStack> predicate);

    /**
     * Adds the full item stack to the first available slot in
     * the storage block.
     *
     * @param stack The ItemStack to insert
     * @return if the full transfer was successful
     */
    protected abstract boolean insertFullStackImpl(final ItemStack stack);


    /**
     * Get the class type of this storage block. Used for serialization and
     * deserialization.
     * 
     * @return The class type of this storage block.
     */
    public abstract Class<? extends AbstractStorageBlock> getStorageBlockClass();
}
