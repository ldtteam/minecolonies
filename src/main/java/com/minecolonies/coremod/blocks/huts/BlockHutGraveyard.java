package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

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
    public String getHutName() { return "blockhutgraveyard"; }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.graveyard;
    }
}
