package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.interfaces.IRSComponentBlock;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the PostBox. No different from {@link AbstractBlockHut}
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox> implements IRSComponentBlock
{
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    private static final VoxelShape SHAPE_EAST  = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_WEST  = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.postBox.get();
    }

    @Deprecated
    public float getDestroyProgress(final BlockState state, @NotNull final Player player, @NotNull final BlockGetter world, @NotNull final BlockPos pos)
    {
        return 1 / 30f;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        switch (state.getValue(FACING))
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
