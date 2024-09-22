package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.function.Predicate;

public interface IStorageBlockInterface
{
    /**
     * Whether the storageblock should be included in building containers automatically.
     *
     * @param building The building that is being constructed.
     * @return Whether the storageblock should be included in building containers automatically.
     */
    boolean shouldAutomaticallyAdd(final IBuilding building);

    /**
     * Sets whether the block is part of a warehouse.
     *
     * @param inWarehouse Whether it's in a warehouse
     */
    void setInWarehouse(final boolean inWarehouse);

    /**
     * Gets the current upgrade level of the storageblock
     *
     * @return The current level
     */
    int getUpgradeLevel();

    /**
     * Upgrades the size of the storage, if applicable.
     */
    void increaseUpgradeLevel();

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param storage The item to check for
     */
    int getCount(final ItemStorage storage);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param predicate The predicate used to select items
     */
    int getItemCount(final Predicate<ItemStack> predicate);

    /**
     * Return the number of free slots in the container.
     *
     * @return The free slots
     */
    int getFreeSlots();

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    Map<ItemStorage, Integer> getAllContent();

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    int getCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    boolean isEmpty();

    /**
     * Get the modifiable ItemHandler for the given storageblock
     *
     * @return the itemhandler
     */
    IItemHandlerModifiable getInventory();

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
     * Sets the block position of the building this storage belongs to
     *
     * @param pos The position of the building
     */
    void setBuildingPos(final BlockPos pos);
}
