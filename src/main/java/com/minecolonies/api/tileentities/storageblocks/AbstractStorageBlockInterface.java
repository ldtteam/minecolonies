package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The abstract StorageBlockInterface class that all implementations should
 * inherit from.
 */
public abstract class AbstractStorageBlockInterface
{
    /**
     * The position of the block entity in the world.
     */
    protected final BlockPos targetPos;

    /**
     * The level that the block entity is located in.
     */
    protected final Level level;

    /**
     * Constructor
     *
     * @param targetPos The location of the block
     * @param level     The world the block is in
     */
    public AbstractStorageBlockInterface(final BlockPos targetPos, Level level)
    {
        this.targetPos = targetPos;
        this.level = level;
    }

    /**
     * Check whether the position is still valid for this storage interface
     *
     * @return Whether the position is still valid
     */
    public abstract boolean isStillValid();

    /**
     * Whether the storageblock should be included in building containers automatically.
     *
     * @param building The building that is being constructed.
     * @return Whether the storageblock should be included in building containers automatically.
     */
    public abstract boolean shouldAutomaticallyAdd(final IBuilding building);

    /**
     * Sets whether the block is part of a warehouse.
     *
     * @param inWarehouse Whether it's in a warehouse
     */
    public abstract void setInWarehouse(final boolean inWarehouse);

    /**
     * Gets the current upgrade level of the storageblock
     *
     * @return The current level
     */
    public abstract int getUpgradeLevel();

    /**
     * Upgrades the size of the storage, if applicable.
     */
    public abstract void increaseUpgradeLevel();

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param storage The item to check for
     */
    public abstract int getCount(final ItemStorage storage);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param predicate The predicate used to select items
     */
    public abstract int getItemCount(final Predicate<ItemStack> predicate);

    /**
     * Return the number of free slots in the container.
     *
     * @return The free slots
     */
    public abstract int getFreeSlots();

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    public abstract Map<ItemStorage, Integer> getAllContent();

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    public abstract int getCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    public abstract boolean isEmpty();

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
     * Sets the block position of the building this storage belongs to
     *
     * @param pos The position of the building
     */
    public abstract void setBuildingPos(final BlockPos pos);

    /**
     * The position of the target storage block.
     *
     * @return The position.
     */
    public BlockPos getPosition()
    {
        return targetPos;
    }

    /**
     * Whether the block is currently loaded.
     *
     * @return Whether it's loaded.
     */
    public boolean isLoaded()
    {
        return WorldUtil.isBlockLoaded(level, targetPos);
    }

    /**
     * Add an item stack to this storage block.
     *
     * @param stack The stack to add
     * @return Whether the addition was successful
     */
    public abstract boolean storeItemStack(@NotNull final ItemStack stack);

    /**
     * Get any matching item stacks within the storage block.
     *
     * @param predicate The predicate to test against
     * @return The list of matching item stacks
     */
    public abstract List<ItemStack> getMatching(@NotNull final Predicate<ItemStack> predicate);

    /**
     * Force stack to the storage block.
     *
     * @param itemStack                ItemStack to add.
     * @param itemStackToKeepPredicate The {@link Predicate} that determines which ItemStacks to keep in the inventory. Return false to replace.
     * @return itemStack which has been replaced, null if none has been replaced.
     */
    @Nullable
    public abstract ItemStack forceAddItemStack(
      @NotNull final ItemStack itemStack,
      @NotNull final Predicate<ItemStack> itemStackToKeepPredicate);

    /**
     * Method to transfer an ItemStacks from the given source {@link IItemHandler} to the Storage Block.
     *
     * @param targetHandler The {@link IItemHandler} that works as receiver.
     * @param predicate     the predicate for the stack.
     * @return true when the swap was successful, false when not.
     */
    public abstract boolean transferItemStackFromStorageIntoNextBestSlot(
      @NotNull final IItemHandler targetHandler,
      final Predicate<ItemStack> predicate);

    /**
     * Method to swap the ItemStacks from storage to the given target {@link IItemHandler}.
     *
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @param stackPredicate The type of stack to pickup.
     * @param count          how much to pick up.
     * @return True when the swap was successful, false when not.
     */
    public abstract boolean transferItemStackFromStorageIntoNextFreeSlot(
      @NotNull final IItemHandler targetHandler,
      @NotNull final Predicate<ItemStack> stackPredicate,
      final int count);

    /**
     * Method to swap the ItemStacks from the given source {@link IItemHandler} to storage. Trying to merge existing itemStacks if possible.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param sourceIndex   The index of the slot that is being extracted from.
     * @return True when the swap was successful, false when not.
     */
    public abstract boolean transferFromIndexToStorageIntoNextBestSlot(
      @NotNull final IItemHandler sourceHandler,
      final int sourceIndex);
}
