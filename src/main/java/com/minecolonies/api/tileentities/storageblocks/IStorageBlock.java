package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * An interface used for defining storage blocks. This shouldn't be used
 * directly. Use the AbstractStorageBlock instead.
 */
public interface IStorageBlock
{
    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param storage The item to check for
     */
    int getItemCount(final ItemStorage storage);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param predicate The predicate used to select items
     */
    int getItemCount(final Predicate<ItemStack> predicate);

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    int getItemCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Check whether the position is still valid for this storage interface
     *
     * @param building The building the block is in
     * @return Whether the position is still valid
     */
    boolean isStillValid(final IBuilding building);

    /**
     * Check whether the position is still valid for this storage interface
     *
     * @param building A view of the building the block is in
     * @return Whether the position is still valid
     */
    boolean isStillValid(final IBuildingView building);

    /**
     * Return the number of free slots in the container.
     *
     * @return The free slots
     */
    int getFreeSlots();

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    boolean isEmpty();

    /**
     * Whether the storage block has 0 completely free slots.
     *
     * @return True if there are no free slots, false otherwise.
     */
    boolean isFull();

    /**
     * Return whether the storageblock contains a matching item stack
     *
     * @param stack        The item type to compare
     * @param count        The amount that must be present
     * @param ignoreDamage Whether the items should have matching damage values
     * @return Whether the storageblock contains the match
     */
    boolean hasItemStack(final ItemStack stack, final int count, final boolean ignoreDamage);

    /**
     * Return whether the storageblock contains any items matching the predicate
     *
     * @param predicate The predicate to check against
     * @return Whether the storageblock has any matches
     */
    boolean hasItemStack(final Predicate<ItemStack> predicate);

    /**
     * Get any matching item stacks within the storage block.
     *
     * @param predicate The predicate to test against
     * @return The list of matching item stacks
     */
    List<ItemStack> getMatching(@NotNull final Predicate<ItemStack> predicate);

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    Map<ItemStorage, Integer> getAllContent();

    /**
     * Removes an item stack matching the given predicate from the storage block
     * and returns it.
     *
     * @param predicate The predicate to match
     * @param simulate If true, actually remove the item.
     * @return The matching item stack, or ItemStack.EMPTY
     */
    ItemStack extractItem(final Predicate<ItemStack> predicate, boolean simulate);

    /**
     * Removes an item stack matching the given predicate from the storage block
     * and returns it. Will only return if the stack has at least minCount stack size.
     *
     * @param predicate The predicate to match
     * @param minCount The minimum count the stack size must be
     * @param simulate If true, actually removes the item.
     * @return The matching item stack, or ItemStack.EMPTY
     */
    ItemStack extractItem(final Predicate<ItemStack> predicate, int minCount, boolean simulate);

    /**
     * Adds the full item stack to the first available slot in
     * the storage block.
     *
     * @return if the full transfer was successful
     */
    boolean insertFullStackImpl(final ItemStack stack, final boolean simulate);
}
