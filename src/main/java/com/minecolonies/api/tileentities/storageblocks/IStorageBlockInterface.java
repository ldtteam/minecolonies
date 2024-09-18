package com.minecolonies.api.tileentities.storageblocks;

import net.minecraft.world.level.block.entity.BlockEntity;

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
    void setInWarehouse(BlockEntity blockEntity, boolean inWarehouse);

    /**
     * Gets the current upgrade level of the storageblock
     *
     * @param blockEntity The blockentity to get the level of
     * @return The current level
     */
    int getUpgradeLevel(BlockEntity blockEntity);

    /**
     * Upgrades the size of the storage, if applicable.
     *
     * @param blockEntity The blockentity to increase the level of
     */
    void increaseUpgradeLevel(BlockEntity blockEntity);
}
