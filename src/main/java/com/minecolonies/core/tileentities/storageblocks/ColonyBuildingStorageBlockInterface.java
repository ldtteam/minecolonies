package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlockInterface;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class ColonyBuildingStorageBlockInterface extends RackStorageBlockInterface
{
    /**
     * Constructor
     *
     * @param pos The position of the target storage block
     * @param world The level the storage block is in
     */
    public ColonyBuildingStorageBlockInterface(BlockPos pos, Level world)
    {
        super(pos, world);

        BlockEntity targetBlockEntity = world.getBlockEntity(pos);

        if (!(targetBlockEntity instanceof TileEntityColonyBuilding))
        {
            throw new IllegalArgumentException("The block at the target position must be an instance of TileEntityColonyBuilding");
        }
    }

    /**
     * Increase the level of the storage block. For colony buildings
     * this does nothing.
     */
    @Override
    public void increaseUpgradeLevel()
    {
        // Do nothing for colony buildings.
    }
}
