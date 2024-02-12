package com.minecolonies.core.blocks.decorative;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.minecolonies.api.blocks.decorative.AbstractColonyFlagBanner;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A custom banner block to construct the associated tile entity that will render the colony flag.
 * This is the wall version. For the floor version: {@link BlockColonyFlagBanner}
 */
public class BlockColonyFlagWallBanner extends AbstractColonyFlagBanner<BlockColonyFlagWallBanner>
{
    public static final MapCodec<BlockColonyFlagWallBanner> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(DyeColor.CODEC.fieldOf("color").forGetter(BlockColonyFlagWallBanner::getColor),
            propertiesCodec())
        .apply(builder, BlockColonyFlagWallBanner::new));
    public static final DirectionProperty          HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> BANNER_SHAPES     = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D),
            Direction.SOUTH, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D),
            Direction.WEST,  Block.box(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D),
            Direction.EAST,  Block.box(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));

    public BlockColonyFlagWallBanner()
    {
        this(DyeColor.WHITE,
            Properties.of().mapColor(MapColor.WOOD)
              .sound(SoundType.WOOD)
                .noCollission()
                .strength(1F)
                .sound(SoundType.WOOD));
    }

    public BlockColonyFlagWallBanner(final DyeColor dyeColor, final Properties properties)
    {
        super(dyeColor, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlockColonyFlagWallBanner> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId() { return this.asItem().getDescriptionId(); }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos.relative(state.getValue(HORIZONTAL_FACING).getOpposite())).isSolid();
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return facing == stateIn.getValue(HORIZONTAL_FACING).getOpposite() && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return BANNER_SHAPES.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState blockstate = this.defaultBlockState();
        LevelReader iworldreader = context.getLevel();
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HORIZONTAL_FACING); }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, REGISTRY_NAME_WALL);
    }
}
