package com.minecolonies.coremod.blocks.decorative;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.minecolonies.api.blocks.decorative.AbstractColonyFlagBanner;
import net.minecraft.block.*;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import java.util.Map;

/**
 * A custom banner block to construct the associated tile entity that will render the colony flag.
 * This is the wall version. For the floor version: {@link BlockColonyFlagBanner}
 */
public class BlockColonyFlagWallBanner extends AbstractColonyFlagBanner<BlockColonyFlagWallBanner>
{
    public static final DirectionProperty          HORIZONTAL_FACING = HorizontalBlock.FACING;
    private static final Map<Direction, VoxelShape> BANNER_SHAPES     = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D),
            Direction.SOUTH, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D),
            Direction.WEST,  Block.box(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D),
            Direction.EAST,  Block.box(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));

    public BlockColonyFlagWallBanner()
    {
        super();
        setRegistryName(REGISTRY_NAME_WALL);

        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public String getDescriptionId() { return this.asItem().getDescriptionId(); }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos.relative(state.getValue(HORIZONTAL_FACING).getOpposite())).getMaterial().isSolid();
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return facing == stateIn.getValue(HORIZONTAL_FACING).getOpposite() && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return BANNER_SHAPES.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState blockstate = this.defaultBlockState();
        IWorldReader iworldreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction[] adirection = context.getNearestLookingDirections();

        for(Direction direction : adirection)
        {
            if (direction.getAxis().isHorizontal())
            {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(HORIZONTAL_FACING, direction1);
                if (blockstate.canSurvive(iworldreader, blockpos))
                    return blockstate;
            }
        }

        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(HORIZONTAL_FACING); }
}
