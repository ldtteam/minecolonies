package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.function.Predicate;

public interface IStorageBlockInterface
{
    /**
     * Whether the storageblock should be included in building containers automatically.
     * @return Whether the storageblock should be included in building containers automatically.
     */
    boolean automaticallyAddToBuilding();

    /**
     * Sets whether the block is part of a warehouse.
     *
     * @param blockEntity The blockentity to set
     * @param inWarehouse Whether it's in a warehouse
     */
    void setInWarehouse(final BlockEntity blockEntity, final boolean inWarehouse);

    /**
     * Gets the current upgrade level of the storageblock
     *
     * @param blockEntity The blockentity to get the level of
     * @return The current level
     */
    int getUpgradeLevel(final BlockEntity blockEntity);

    /**
     * Upgrades the size of the storage, if applicable.
     *
     * @param blockEntity The blockentity to increase the level of
     */
    void increaseUpgradeLevel(final BlockEntity blockEntity);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param blockEntity the blockentity to check
     * @param storage     The item to check for
     */
    int getCount(final BlockEntity blockEntity, final ItemStorage storage);

    /**
     * Gets the amount of a particular item contained in the storageblock
     *
     * @param blockEntity the blockentity to check
     * @param predicate   The predicate used to select items
     */
    int getItemCount(final BlockEntity blockEntity, final Predicate<ItemStack> predicate);

    /**
     * Return the number of free slots in the container.
     *
     * @param blockEntity the block entity to check
     * @return The free slots
     */
    int getFreeSlots(final BlockEntity blockEntity);

    /**
     * Gets all items and their count from the storage block.
     *
     * @param blockEntity the block entity to check
     * @return The items and their count
     */
    Map<ItemStorage, Integer> getAllContent(final BlockEntity blockEntity);

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param blockEntity the block entity to check
     * @param stack The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    int getCount(final BlockEntity blockEntity, final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT);
}
