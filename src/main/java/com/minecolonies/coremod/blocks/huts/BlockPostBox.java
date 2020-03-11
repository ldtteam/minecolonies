package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the PostBox.
 * No different from {@link AbstractBlockHut}
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox>
{
    private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);

    @NotNull
    @Override
    public String getName()
    {
        return "blockpostbox";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.postBox;
    }

    @NotNull
    @Override
    public VoxelShape getShape(
      final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        switch (state.get(FACING))
        {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            default:
                return SHAPE_WEST;
        }
    }
}
