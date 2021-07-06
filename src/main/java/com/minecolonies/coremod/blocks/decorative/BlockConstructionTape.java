package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.Plane;

/**
 * This block is used as a border to show the size of the building. It also shows that the building is in the progress of being built.
 */
public class BlockConstructionTape extends AbstractBlockMinecoloniesConstructionTape<BlockConstructionTape>
{
    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockconstructiontape";

    /**
     * Constructor for the Construction Tape decoration.
     */
    public BlockConstructionTape()
    {
        super(Properties.of(Material.REPLACEABLE_PLANT).strength(0.0f).noCollission().noDrops());
        setRegistryName(BLOCK_NAME);

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

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return super.getShape(state, worldIn, pos, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
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
      final IWorld worldIn,
      @NotNull final BlockPos currentPos,
      final BlockPos pos)
    {
        if (stateIn.getValue(WATERLOGGED))
        {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
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
    public static BlockState getPlacementState(@Nullable BlockState state, IBlockReader world, BlockPos pos, Direction face)
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

    public static List<Direction> getConnections(IBlockReader world, BlockPos pos, Direction face, boolean corner)
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

    protected static boolean canConnect(IBlockReader world, BlockPos pos, Direction face)
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

    protected static boolean canRemoveTStem(IBlockReader world, BlockPos pos, Direction face)
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
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @Override
    public void onLand(final World worldIn, final BlockPos pos, final BlockState fallingState, final BlockState hitState, final FallingBlockEntity blockEntity)
    {
        worldIn.setBlockAndUpdate(pos, BlockConstructionTape.getPlacementState(
          fallingState, worldIn, pos, fallingState.getValue(FACING)
        ));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, SOUTH, WEST, FACING, CORNER, WATERLOGGED);
    }
}
