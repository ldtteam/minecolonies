package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutWareHouse extends AbstractBlockHut<BlockHutWareHouse>
{
    public BlockHutWareHouse()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutWareHouse";
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityWareHouse(getBuildingEntry().getRegistryName());
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.wareHouse;
    }
}
