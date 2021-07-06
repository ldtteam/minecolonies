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
    /**
     * tall shape.
     */
    private static final VoxelShape SHAPE = VoxelShapes.create(0.1, 0.1, 0.1, 0.9, 1.9, 0.9);

    public BlockHutGraveyard()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return SHAPE;
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
