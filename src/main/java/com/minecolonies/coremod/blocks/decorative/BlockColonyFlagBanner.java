package com.minecolonies.coremod.blocks.decorative;

import com.google.common.collect.Maps;
import com.minecolonies.api.blocks.decorative.AbstractColonyFlagBanner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import java.util.Map;

public class BlockColonyFlagBanner extends AbstractColonyFlagBanner<BlockColonyFlagBanner>
{
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_0_15;
    private static final VoxelShape SHAPE = Block.makeCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public BlockColonyFlagBanner()
    {
        super();
        setRegistryName(REGISTRY_NAME);

        this.setDefaultState(this.stateContainer.getBaseState().with(ROTATION, Integer.valueOf(0)));
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(ROTATION, Integer.valueOf(MathHelper.floor((double)((180.0F + context.getPlacementYaw()) * 16.0F / 360.0F) + 0.5D) & 15));
    }

    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(ROTATION, Integer.valueOf(rot.rotate(state.get(ROTATION), 16)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.with(ROTATION, Integer.valueOf(mirrorIn.mirrorRotation(state.get(ROTATION), 16)));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

}
