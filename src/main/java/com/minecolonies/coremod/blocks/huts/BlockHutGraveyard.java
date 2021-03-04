package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hut for the graveyard. No different from {@link AbstractBlockHut}
 */

public class BlockHutGraveyard extends AbstractBlockHut<BlockHutGraveyard>
{
    public BlockHutGraveyard()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName() { return "blockhutgraveyard"; }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.graveyard;
    }
}
