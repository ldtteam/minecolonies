package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
        super(Properties.of()
                .mapColor(MapColor.PLANT)
                .sound(SoundType.WOOD)
                .replaceable()
                .pushReaction(PushReaction.DESTROY)
                .ignitedByLava()
                .isRedstoneConductor((state, getter, pos) -> false)
                .forceSolidOff()
                .strength(0.0f).noCollission().noLootTable());

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
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return super.getShape(state, worldIn, pos, context);
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
}
