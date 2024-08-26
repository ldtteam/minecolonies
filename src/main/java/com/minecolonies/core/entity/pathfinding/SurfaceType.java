package com.minecolonies.core.entity.pathfinding;

import com.ldtteam.domumornamentum.block.decorative.FloatingCarpetBlock;
import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.ldtteam.domumornamentum.block.vanilla.TrapdoorBlock;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.util.ShapeUtil;
import com.minecolonies.core.blocks.BlockScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Check if we can walk on a surface, drop into, or neither.
 */
public enum SurfaceType
{
    WALKABLE,
    DROPABLE,
    NOT_PASSABLE;

    /**
     * Is the block solid and can be stood upon.
     *
     * @param blockState Block to check.
     * @param pos        the position.
     * @return true if the block at that location can be walked on.
     */
    @NotNull
    public static SurfaceType getSurfaceType(final BlockGetter world, final BlockState blockState, final BlockPos pos)
    {
        return getSurfaceType(world, blockState, pos, null);
    }

    /**
     * Is the block solid and can be stood upon.
     *
     * @param blockState     Block to check.
     * @param pos            the position.
     * @param pathingOptions the pathing options to consider
     * @return true if the block at that location can be walked on.
     */
    @NotNull
    public static SurfaceType getSurfaceType(final BlockGetter world, final BlockState blockState, final BlockPos pos, @Nullable final PathingOptions pathingOptions)
    {
        final Block block = blockState.getBlock();

        if (PathfindingUtils.isDangerous(blockState))
        {
            if (pathingOptions != null && pathingOptions.canPassDanger())
            {
                if (ShapeUtil.isEmpty(blockState.getCollisionShape(world, pos)))
                {
                    return SurfaceType.DROPABLE;
                }
                return SurfaceType.WALKABLE;
            }

            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof FenceBlock
              || block instanceof FenceGateBlock
              || block instanceof WallBlock
              || block instanceof BlockScarecrow
              || block instanceof BambooStalkBlock
              || block instanceof BambooSaplingBlock
              || block instanceof DoorBlock)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final VoxelShape shape = blockState.getCollisionShape(world, pos);
        final double maxShapeY = ShapeUtil.max(shape, Direction.Axis.Y);
        if (maxShapeY < 0.5 && PathfindingUtils.isDangerous(world.getBlockState(pos.below())))
        {
            if (pathingOptions != null && pathingOptions.canPassDanger())
            {
                return SurfaceType.WALKABLE;
            }

            return SurfaceType.NOT_PASSABLE;
        }

        if ((block instanceof PanelBlock || block instanceof TrapdoorBlock) && !blockState.getValue(TrapdoorBlock.OPEN))
        {
            return SurfaceType.WALKABLE;
        }

        if (maxShapeY > 1.0)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (PathfindingUtils.isWater(world, pos, blockState, fluid))
        {
            return SurfaceType.WALKABLE;
        }

        if (PathfindingUtils.isLava(world, pos, blockState, fluid))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (block instanceof AbstractBlockMinecoloniesConstructionTape || block instanceof SignBlock || block instanceof VineBlock)
        {
            return SurfaceType.DROPABLE;
        }

        if ((BlockUtils.isAnySolid(blockState) && ShapeUtil.max(shape, Direction.Axis.X) - ShapeUtil.min(shape, Direction.Axis.X) > 0.75
               && (ShapeUtil.max(shape, Direction.Axis.Z) - ShapeUtil.min(shape, Direction.Axis.Z)) > 0.75)
              || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowLayerBlock.LAYERS) > 1)
              || block instanceof FloatingCarpetBlock
              || block instanceof CarpetBlock)
        {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }
}
