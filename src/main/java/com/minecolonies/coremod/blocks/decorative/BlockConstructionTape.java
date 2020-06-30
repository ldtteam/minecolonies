package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Direction.*;

/**
 * This block is used as a border to show the size of the building.
 * It also shows that the building is in the progress of being built.
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
        super(Properties.create(Material.TALL_PLANTS).hardnessAndResistance(0.0f).doesNotBlockMovement().noDrops());
        setRegistryName(BLOCK_NAME);

        this.shapes = makeShapes(2,2,16,0,16);

        this.setDefaultState(this.getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
                .with(CORNER, false)
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
                context.getWorld(),
                context.getPos(),
                faces[0].getHorizontalIndex() >= 0 ? faces[0] : faces[1]
        );
    }

    @NotNull
    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction dir, final BlockState state, final IWorld worldIn, @NotNull final BlockPos currentPos, final BlockPos pos)
    {
        if (stateIn.get(WATERLOGGED))
        {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return BlockConstructionTape.getPlacementState(
                super.updatePostPlacement(stateIn, dir, state, worldIn, currentPos, pos), worldIn, currentPos, stateIn.get(FACING)
        );
    }

    /**
     * A static version of getStateForPlacement to allow helpers to interact with states
     * @param state the block state to configure
     * @param world the world
     * @param pos the position of the new state
     * @param face the default direction of the tape when there are no connections
     * @return the configured state
     */
    public static BlockState getPlacementState (@Nullable BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        Fluid fluid = world.getFluidState(pos).getFluid();
        List<Direction> connections = getConnections(world, pos, face, state.get(CORNER));

        return state
                .with(NORTH, connections.contains(Direction.NORTH))
                .with(EAST,  connections.contains(Direction.EAST))
                .with(SOUTH, connections.contains(Direction.SOUTH))
                .with(WEST,  connections.contains(Direction.WEST))
                .with(FACING, face)
                .with(WATERLOGGED, fluid == Fluids.WATER);
    }

    public static List<Direction> getConnections (IBlockReader world, BlockPos pos, Direction face, boolean corner)
    {
        List<Direction> connections = new ArrayList<>();

        for (Direction direction : Plane.HORIZONTAL)
            if (canConnect(world, pos, direction))
                connections.add(direction);

        // When the tape is isolated, set it to its default orientation
        // considering whether it is a corner
        if (connections.size() == 0 || (connections.size() == 1 && corner))
        {
            if (corner)
            {
                connections.clear();
                connections.add(face);
                connections.add(face.rotateY());
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
                if (!connections.contains(direction))
                    stem = direction.getOpposite();

            // If the block in the direction of the stem also has three connections
            // with it's stem facing this block, remove this block's stem
            if (canRemoveTStem(world, pos, stem))
                connections.remove(connections.indexOf(stem));
        }

        return connections;
    }

    protected static boolean canConnect (IBlockReader world, BlockPos pos, Direction face)
    {
        BlockPos adjacent;
        switch (face)
        {
            default:
            case NORTH: adjacent = pos.north(); break;
            case EAST:  adjacent = pos.east(); break;
            case SOUTH: adjacent = pos.south(); break;
            case WEST:  adjacent = pos.west(); break;
        }
        return world.getBlockState(adjacent).getBlock() instanceof BlockConstructionTape;
    }

    protected static boolean canRemoveTStem (IBlockReader world, BlockPos pos, Direction face)
    {
        BlockState neighbor = world.getBlockState(pos.offset(face));
        switch (face)
        {
            case NORTH:
                return !neighbor.get(NORTH) && neighbor.get(EAST) && neighbor.get(WEST);
            case EAST:
                return !neighbor.get(EAST) && neighbor.get(NORTH) && neighbor.get(SOUTH);
            case SOUTH:
                return !neighbor.get(SOUTH) && neighbor.get(EAST) && neighbor.get(WEST);
            case WEST:
                return !neighbor.get(WEST) && neighbor.get(NORTH) && neighbor.get(SOUTH);
        }
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @Override
    public void onEndFalling(World worldIn, BlockPos pos, BlockState fallingState, BlockState hitState)
    {
        worldIn.setBlockState(pos, BlockConstructionTape.getPlacementState(
                fallingState, worldIn, pos, fallingState.get(FACING)
        ));

        updateNeighbors(fallingState, worldIn, pos, 3);
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, SOUTH, WEST, FACING, CORNER, WATERLOGGED);
    }
}
