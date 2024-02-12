package com.minecolonies.core.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractColonyFlagBanner;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

/**
 * A custom banner block to construct the associated tile entity that will render the colony flag.
 * This is the floor version. For the wall version: {@link BlockColonyFlagWallBanner}
 */
public class BlockColonyFlagBanner extends AbstractColonyFlagBanner<BlockColonyFlagBanner>
{
    public static final MapCodec<BlockColonyFlagBanner> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(DyeColor.CODEC.fieldOf("color").forGetter(BlockColonyFlagBanner::getColor),
            propertiesCodec())
        .apply(builder, BlockColonyFlagBanner::new));
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public BlockColonyFlagBanner()
    {
        super(DyeColor.WHITE,
            Properties.of().mapColor(MapColor.WOOD)
              .sound(SoundType.WOOD)
                .noCollission()
                .strength(1F)
                .sound(SoundType.WOOD));
    }

    public BlockColonyFlagBanner(final DyeColor dyeColor, final Properties properties)
    {
        super(dyeColor, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)));
    }

    @Override
    protected MapCodec<BlockColonyFlagBanner> codec()
    {
        return CODEC;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos.below()).isSolid();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(ROTATION, Integer.valueOf(Mth.floor((double)((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(ROTATION, Integer.valueOf(rot.rotate(state.getValue(ROTATION), 16)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.setValue(ROTATION, Integer.valueOf(mirrorIn.mirror(state.getValue(ROTATION), 16)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, REGISTRY_NAME);
    }
}
