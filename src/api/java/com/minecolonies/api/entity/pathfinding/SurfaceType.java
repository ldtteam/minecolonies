package com.minecolonies.api.entity.pathfinding;

import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
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
    public static SurfaceType getSurfaceType(final IBlockReader world, final BlockState blockState, final BlockPos pos)
    {
        final Block block = blockState.getBlock();
        if (block instanceof FenceBlock
              || block instanceof FenceGateBlock
              || block instanceof WallBlock
              || block instanceof FireBlock
              || block instanceof CampfireBlock
              || block instanceof AbstractBlockMinecoloniesDefault
              || block instanceof AbstractBlockBarrel
              || block instanceof BambooBlock
              || block instanceof DoorBlock
              || block instanceof MagmaBlock)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final VoxelShape shape = blockState.getShape(world, pos);
        if (shape.max(Direction.Axis.Y) > 1.0)
        {
            return SurfaceType.NOT_PASSABLE;
        }

        final FluidState fluid = world.getFluidState(pos);
        if (blockState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)))
        {
            return SurfaceType.NOT_PASSABLE;
        }

        if (isWater(world, pos, blockState, fluid))
        {
            return SurfaceType.WALKABLE;
        }

        if (block instanceof AbstractBlockMinecoloniesConstructionTape || block instanceof AbstractSignBlock || block instanceof VineBlock)
        {
            return SurfaceType.DROPABLE;
        }

        if ((blockState.getMaterial().isSolid() && (shape.max(Direction.Axis.X) - shape.min(Direction.Axis.X)) > 0.75
               && (shape.max(Direction.Axis.Z) - shape.min(Direction.Axis.Z)) > 0.75)
              || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowBlock.LAYERS) > 1)
              || block instanceof BlockFloatingCarpet
              || block instanceof CarpetBlock)
        {
            return SurfaceType.WALKABLE;
        }

        return SurfaceType.DROPABLE;
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos the pos in the world.
     * @return true if so.
     */
    public static boolean isWater(@NotNull final IWorldReader world, final BlockPos pos)
    {
        return isWater(world, pos, null, null);
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     *
     * @param pos         the pos in the world.
     * @param pState      existing blockstate or null
     * @param pFluidState existing fluidstate or null
     * @return true if so.
     */
    public static boolean isWater(@NotNull final IBlockReader world, final BlockPos pos, @Nullable BlockState pState, @Nullable FluidState pFluidState)
    {
        BlockState state = pState;
        if (state == null)
        {
            state = world.getBlockState(pos);
        }

        if (state.canOcclude())
        {
            return false;
        }
        if (state.getBlock() == Blocks.WATER)
        {
            return true;
        }

        FluidState fluidState = pFluidState;
        if (fluidState == null)
        {
            fluidState = world.getFluidState(pos);
        }

        if (fluidState == null || fluidState.isEmpty())
        {
            return false;
        }

        final Fluid fluid = fluidState.getType();
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }
}
