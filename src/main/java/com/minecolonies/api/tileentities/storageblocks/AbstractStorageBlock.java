package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
     * The level that the block entity is located in.
     */
    protected final Level level;

    /**
     * The level that the storage block has been upgraded to.
     */
    protected int upgradeLevel = 0;

    /**
     * The notifier used to let other blocks know when this
     * storageblock has items inserted.
     */
    protected InsertNotifier insertNotifier;

    /**
     * Constructor
     *
     * @param targetPos The location of the block
     * @param level     The world the block is in
     */
    public AbstractStorageBlock(final BlockPos targetPos, Level level)
    {
        this.targetPos = targetPos;
        this.level = level;
        insertNotifier = new InsertNotifier(level);
    }

    /**
     * Add a new listener for when items are inserted
     * to this storage block.
     *
     * @param listenerPos The position of the listener entity
     */
    public void addInsertListener(BlockPos listenerPos)
    {
        insertNotifier.addInsertListener(listenerPos);
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

    /**
     * Whether the block is currently loaded.
     *
     * @return Whether it's loaded.
     */
    public final boolean isLoaded()
    {
        return WorldUtil.isBlockLoaded(level, targetPos);
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
        result.put(TAG_INPUT_LISTENER, insertNotifier.serializeToNBT());

        return result;
    }

    /**
     * Load block data from NBT data.
     *
     * @param nbt The NBT data for the block
     */
    public void read(final CompoundTag nbt)
    {
        targetPos = BlockPosUtil.read(nbt, TAG_POS);
        insertNotifier.read(nbt.getCompound(TAG_INPUT_LISTENER));
    }

    /**
     * Adds the full item stack to the first available slot in
     * the storage block.
     *
     * @return if the full transfer was successful
     */
    public boolean insertFullStack(final ItemStack stack, final boolean simulate)
    {
        final boolean result = insertFullStackImpl(stack, simulate);

        if (result && !simulate)
        {
            insertNotifier.notifyInsert(targetPos, stack);
        }

        return result;
    }

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
     * and returns it. Will only return if the stack has at least minCount stack size.
     *
     * @param predicate The predicate to match
     * @param minCount The minimum count the stack size must be
     * @param simulate If true, actually removes the item.
     * @return The matching item stack, or ItemStack.EMPTY
     */
    public abstract ItemStack extractItem(final Predicate<ItemStack> predicate, int minCount, boolean simulate);

    /**
     * Adds the full item stack to the first available slot in
     * the storage block.
     *
     * @return if the full transfer was successful
     */
    protected abstract boolean insertFullStackImpl(final ItemStack stack, final boolean simulate);
}
