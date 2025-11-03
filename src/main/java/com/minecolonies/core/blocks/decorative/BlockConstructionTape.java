package com.minecolonies.core.blocks.decorative;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This block is used as a border to show the size of the building. It also shows that the building is in the progress of being built.
 */
public class BlockConstructionTape extends FallingBlock implements SimpleWaterloggedBlock
{
    public static final MapCodec<BlockConstructionTape> CODEC = simpleCodec(BlockConstructionTape::new);

    public static final BooleanProperty         NORTH       = PipeBlock.NORTH;
    public static final BooleanProperty         EAST        = PipeBlock.EAST;
    public static final BooleanProperty         SOUTH       = PipeBlock.SOUTH;
    public static final BooleanProperty         WEST        = PipeBlock.WEST;
    public static final BooleanProperty         WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction> FACING      = HorizontalDirectionalBlock.FACING;

    protected VoxelShape[] shapes = new VoxelShape[] {};

    /**
     * Implies that the tape should revert to a corner if there are no connections. Must be set explicitly. For use by the builder handler.
     */
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");

    public BlockConstructionTape(final Properties properties)
    {
        super(properties);
        this.shapes = makeShapes(2, 2, 16, 0, 16);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(CORNER, false)
        );
    }

    @Override
    protected MapCodec<BlockConstructionTape> codec()
    {
        return CODEC;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return this.shapes[this.getIndex(state)];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        // Get the closest horizontal axis for the default orientation
        Direction[] faces = context.getNearestLookingDirections();

        return BlockConstructionTape.getPlacementState(
          super.getStateForPlacement(context),
          context.getLevel(),
          context.getClickedPos(),
          faces[0].get2DDataValue() >= 0 ? faces[0] : faces[1]
        );
    }

    @NotNull
    @Override
    public BlockState updateShape(
      @NotNull final BlockState stateIn,
      final Direction dir,
      final BlockState state,
      final LevelAccessor worldIn,
      @NotNull final BlockPos currentPos,
      final BlockPos pos)
    {
        if (stateIn.getValue(WATERLOGGED))
        {
            worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return BlockConstructionTape.getPlacementState(
          super.updateShape(stateIn, dir, state, worldIn, currentPos, pos), worldIn, currentPos, stateIn.getValue(FACING)
        );
    }

    /**
     * A static version of getStateForPlacement to allow helpers to interact with states
     *
     * @param state the block state to configure
     * @param world the world
     * @param pos   the position of the new state
     * @param face  the default direction of the tape when there are no connections
     * @return the configured state
     */
    public static BlockState getPlacementState(@Nullable BlockState state, BlockGetter world, BlockPos pos, Direction face)
    {
        Fluid fluid = world.getFluidState(pos).getType();
        List<Direction> connections = getConnections(world, pos, face, state.getValue(CORNER));

        return state
                 .setValue(NORTH, connections.contains(Direction.NORTH))
                 .setValue(EAST, connections.contains(Direction.EAST))
                 .setValue(SOUTH, connections.contains(Direction.SOUTH))
                 .setValue(WEST, connections.contains(Direction.WEST))
                 .setValue(FACING, face)
                 .setValue(WATERLOGGED, fluid == Fluids.WATER);
    }

    public static List<Direction> getConnections(BlockGetter world, BlockPos pos, Direction face, boolean corner)
    {
        List<Direction> connections = new ArrayList<>();

        for (Direction direction : Plane.HORIZONTAL)
        {
            if (canConnect(world, pos, direction))
            {
                connections.add(direction);
            }
        }

        // When the tape is isolated, set it to its default orientation
        // considering whether it is a corner
        if (connections.size() == 0 || (connections.size() == 1 && corner))
        {
            if (corner)
            {
                connections.clear();
                connections.add(face);
                connections.add(face.getClockWise());
            }
            else
            {
                connections.add(face.getAxis() == Axis.X ? Direction.SOUTH : Direction.EAST);
                connections.add(face.getAxis() == Axis.X ? Direction.NORTH : Direction.WEST);
            }
        }
        else if (connections.size() == 1)
        {
            connections.add(connections.get(0).getOpposite());
        }
        else if (connections.size() == 3)
        {
            Direction stem = Direction.NORTH;

            for (Direction direction : Plane.HORIZONTAL)
            {
                if (!connections.contains(direction))
                {
                    stem = direction.getOpposite();
                }
            }

            // If the block in the direction of the stem also has three connections
            // with it's stem facing this block, remove this block's stem
            if (canRemoveTStem(world, pos, stem))
            {
                connections.remove(connections.indexOf(stem));
            }
        }

        return connections;
    }

    protected static boolean canConnect(BlockGetter world, BlockPos pos, Direction face)
    {
        BlockPos adjacent;
        switch (face)
        {
            default:
            case NORTH:
                adjacent = pos.north();
                break;
            case EAST:
                adjacent = pos.east();
                break;
            case SOUTH:
                adjacent = pos.south();
                break;
            case WEST:
                adjacent = pos.west();
                break;
        }
        return world.getBlockState(adjacent).getBlock() instanceof BlockConstructionTape;
    }

    protected static boolean canRemoveTStem(BlockGetter world, BlockPos pos, Direction face)
    {
        BlockState neighbor = world.getBlockState(pos.relative(face));
        switch (face)
        {
            case NORTH:
                return !neighbor.getValue(NORTH) && neighbor.getValue(EAST) && neighbor.getValue(WEST);
            case EAST:
                return !neighbor.getValue(EAST) && neighbor.getValue(NORTH) && neighbor.getValue(SOUTH);
            case SOUTH:
                return !neighbor.getValue(SOUTH) && neighbor.getValue(EAST) && neighbor.getValue(WEST);
            case WEST:
                return !neighbor.getValue(WEST) && neighbor.getValue(NORTH) && neighbor.getValue(SOUTH);
        }
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @Override
    public void onLand(final Level worldIn, final BlockPos pos, final BlockState fallingState, final BlockState hitState, final FallingBlockEntity blockEntity)
    {
        worldIn.setBlockAndUpdate(pos, BlockConstructionTape.getPlacementState(
          fallingState, worldIn, pos, fallingState.getValue(FACING)
        ));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, SOUTH, WEST, FACING, CORNER, WATERLOGGED);
    }

    protected int getIndex(BlockState state)
    {
        int i = 0;
        if (state.getValue(NORTH))
        {
            i |= getMask(Direction.NORTH);
        }

        if (state.getValue(EAST))
        {
            i |= getMask(Direction.EAST);
        }

        if (state.getValue(SOUTH))
        {
            i |= getMask(Direction.SOUTH);
        }

        if (state.getValue(WEST))
        {
            i |= getMask(Direction.WEST);
        }

        return i;
    }

    private static int getMask(Direction facing)
    {
        return 1 << facing.get2DDataValue();
    }

    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected VoxelShape[] makeShapes(float nodeWidth, float limbWidth, float nodeHeight, float limbBase, float limbTop)
    {
        float nodeStart = 8.0F - nodeWidth;
        float nodeEnd = 8.0F + nodeWidth;
        float limbStart = 8.0F - limbWidth;
        float limbEnd = 8.0F + limbWidth;

        VoxelShape node = Block.box(nodeStart, 0.0F, nodeStart, nodeEnd, nodeHeight, nodeEnd);
        VoxelShape north = Block.box(limbStart, limbBase, 0.0F, limbEnd, limbTop, limbEnd);
        VoxelShape south = Block.box(limbStart, limbBase, limbStart, limbEnd, limbTop, 16.0D);
        VoxelShape west = Block.box(0.0F, limbBase, limbStart, limbEnd, limbTop, limbEnd);
        VoxelShape east = Block.box(limbStart, limbBase, limbStart, 16.0D, limbTop, limbEnd);
        VoxelShape cornernw = Shapes.or(north, east);
        VoxelShape cornerse = Shapes.or(south, west);

        // All 16 possible block combinations, in a specific index to be retrieved by getIndex
        VoxelShape[] avoxelshape = new VoxelShape[]
            {
                Shapes.empty(), south, west, cornerse, north,
                Shapes.or(south, north),
                Shapes.or(west, north),
                Shapes.or(cornerse, north), east,
                Shapes.or(south, east),
                Shapes.or(west, east),
                Shapes.or(cornerse, east), cornernw,
                Shapes.or(south, cornernw),
                Shapes.or(west, cornernw),
                Shapes.or(cornerse, cornernw)
            };

        // Combine the arm voxel shapes with the main node for all combinations
        for (int i = 0; i < 16; ++i)
        {
            avoxelshape[i] = Shapes.or(node, avoxelshape[i]);
        }

        return avoxelshape;
    }
}
