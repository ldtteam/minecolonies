package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeBlockState;
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
     * Start of the collision box at y.
     */
    private static final double BOTTOM_COLLISION = 0.0;

    /**
     * Start of the collision box at x facing South/North.
     */
    private static final double SN_START_COLLISION_X = 0.0;

    /**
     * End of the collision box facing South/North.
     */
    private static final double SN_END_COLLISION_X = 1.0;

    /**
     * Start of the collision box at z facing South/North.
     */
    private static final double SN_START_COLLISION_Z = 0.3375;

    /**
     * End of the collision box facing South/North.
     */
    private static final double SN_END_COLLISION_Z   = 0.6375;
    /**
     * Start of the collision box at x facing South/North.
     */
    private static final double WE_START_COLLISION_X = 0.3375;

    /**
     * End of the collision box facing South/North.
     */
    private static final double WE_END_COLLISION_X = 0.6375;

    /**
     * Start of the collision box at z facing South/North.
     */
    private static final double WE_START_COLLISION_Z = 0.0;

    /**
     * End of the collision box facing South/North.
     */
    private static final double WE_END_COLLISION_Z = 1.0;

    /**
     * Height of the collision box.
     */
    private static final double HEIGHT_COLLISION = 1.0;

    /**
     * Start of the collision box at x facing North.
     */
    private static final double N_START_COLLISION_X = 0.0;

    /**
     * End of the collision box facing North.
     */
    private static final double N_END_COLLISION_X = 0.6375;

    /**
     * Start of the collision box at z facing North.
     */
    private static final double N_START_COLLISION_Z = 0.0;

    /**
     * End of the collision box facing North.
     */
    private static final double N_END_COLLISION_Z = 0.6375;

    /**
     * Start of the collision box at x facing West.
     */
    private static final double W_START_COLLISION_X = 0.0;

    /**
     * Start of the collision box at z facing West.
     */
    private static final double W_START_COLLISION_Z = 0.3375;

    /**
     * End of the collision box facing West.
     */
    private static final double W_END_COLLISION_Z = 1.0;

    /**
     * Start of the collision box at x facing South.
     */
    private static final double S_START_COLLISION_X = 0.3375;

    /**
     * End of the collision box facing South.
     */
    private static final double S_END_COLLISION_X = 1.0;

    /**
     * Start of the collision box at z facing South.
     */
    private static final double S_START_COLLISION_Z = 0.3375;

    /**
     * End of the collision box facing South.
     */
    private static final double S_END_COLLISION_Z = 1.0;

    /**
     * Start of the collision box at x facing East.
     */
    private static final double E_START_COLLISION_X = 0.3375;

    /**
     * End of the collision box facing East.
     */
    private static final double E_END_COLLISION_X = 1.0;

    /**
     * Start of the collision box at z facing East.
     */
    private static final double E_START_COLLISION_Z = 0.0;

    /**
     * End of the collision box facing West.
     */
    private static final double W_END_COLLISION_X = 0.6375;

    /**
     * End of the collision box facing East.
     */
    private static final double E_END_COLLISION_Z = 0.6375;

    private static final VoxelShape SHAPE_NORTH = VoxelShapes.create(N_START_COLLISION_X, BOTTOM_COLLISION, N_START_COLLISION_Z, N_END_COLLISION_X, HEIGHT_COLLISION, N_END_COLLISION_Z);
    private static final VoxelShape SHAPE_WEST  = VoxelShapes.create(W_START_COLLISION_X, BOTTOM_COLLISION, W_START_COLLISION_Z, W_END_COLLISION_X, HEIGHT_COLLISION, W_END_COLLISION_Z);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(S_START_COLLISION_X, BOTTOM_COLLISION, S_START_COLLISION_Z, S_END_COLLISION_X, HEIGHT_COLLISION, S_END_COLLISION_Z);;
    private static final VoxelShape SHAPE_EAST  = VoxelShapes.create(E_START_COLLISION_X, BOTTOM_COLLISION, E_START_COLLISION_Z, E_END_COLLISION_X, HEIGHT_COLLISION, E_END_COLLISION_Z);
    private static final VoxelShape EAST_WEST   = VoxelShapes.create(WE_START_COLLISION_X, BOTTOM_COLLISION, WE_START_COLLISION_Z, WE_END_COLLISION_X, HEIGHT_COLLISION, WE_END_COLLISION_Z);
    private static final VoxelShape NORTH_SOUTH = VoxelShapes.create(SN_START_COLLISION_X, BOTTOM_COLLISION, SN_START_COLLISION_Z, SN_END_COLLISION_X, HEIGHT_COLLISION, SN_END_COLLISION_Z);

    public static final BooleanProperty NORTH = SixWayBlock.NORTH;
    public static final BooleanProperty EAST = SixWayBlock.EAST;
    public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
    public static final BooleanProperty WEST = SixWayBlock.WEST;
    // Implies that the tape should revert to a corner if there are no connections. Must be set explicitly. For use by the builder handler.
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public Direction orientation = Direction.NORTH;
    private final VoxelShape EMPTY_SHAPE = VoxelShapes.empty();

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTape()
    {
        super(Properties.create(Material.TALL_PLANTS).hardnessAndResistance(0.0f).doesNotBlockMovement().noDrops());
        setRegistryName(BLOCK_NAME);

        this.shapes = makeShapes(2,2,16,0,16);
        this.collisionShapes = new VoxelShape[]{};

        this.setDefaultState(this.getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(FACING, this.orientation)
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

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EMPTY_SHAPE;
    }

    @NotNull
    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction dir, final BlockState state, final IWorld worldIn, @NotNull final BlockPos currentPos, final BlockPos pos)
    {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return BlockConstructionTape.getPlacementState(
                super.updatePostPlacement(stateIn, dir, state, worldIn, currentPos, pos),
                worldIn, currentPos, stateIn.get(FACING)
        );
    }

    /*
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
    /*
    public static BlockState getOptimalStateForPlacement(@NotNull final BlockState state, @NotNull final IWorld world, @NotNull final BlockPos position)
    {
        final boolean[] connectors = new boolean[]{world.getBlockState(position.east()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.west()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.north()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.south()).getBlock() instanceof BlockConstructionTape};

        if((connectors[0] && connectors[2]) || (connectors[0] && connectors[3]) || (connectors[1] && connectors[3]) || (connectors[1] && connectors[2]))
        {
            return state.with(VARIANT, AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER);
        }
        return state.with(VARIANT, AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.STRAIGHT);
    }
    */


    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return true;
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

    public static BlockState getPlacementState (@Nullable BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        Fluid fluid = world.getFluidState(pos).getFluid();
        List<Direction> connections = getConnections(world, pos, face, state.get(CORNER));

        assert state != null;
        return state
                .with(NORTH, connections.contains(Direction.NORTH))
                .with(EAST,  connections.contains(Direction.EAST))
                .with(SOUTH, connections.contains(Direction.SOUTH))
                .with(WEST,  connections.contains(Direction.WEST))
                .with(FACING, face)
                .with(WATERLOGGED, fluid == Fluids.WATER);
    }

    public static List<Direction> getConnections (IBlockReader world, BlockPos pos, Direction face, boolean corner) {
        List<Direction> connections = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (canConnect(world, pos, Direction.byHorizontalIndex(i))) {
                connections.add(Direction.byHorizontalIndex(i));
            }
        }

        // When the tape is isolated, set it to its default orientation
        // considering whether it is a corner
        if (connections.size() == 0 || (connections.size() == 1 && corner)) {
            if (corner) {
                connections.clear();
                connections.add(face);
                connections.add(face.rotateY());
            } else {
                connections.add(face.getAxis() == Axis.X ? Direction.SOUTH : Direction.EAST);
                connections.add(face.getAxis() == Axis.X ? Direction.NORTH : Direction.WEST);
            }
        }
        else if (connections.size() == 1) {
            connections.add(connections.get(0).getOpposite());
        }
        else if (connections.size() == 3) {
            Direction stem = Direction.NORTH;

            for (int i = 0; i < 4; i++)
                if (!connections.contains(Direction.byHorizontalIndex(i)))
                    stem = Direction.byHorizontalIndex(i).getOpposite();

            // If the block in the direction of the stem also has three connections
            // with it's stem facing this block, remove this block's stem
            if (canRemoveTStem(world, pos, stem))
                connections.remove(connections.indexOf(stem));
        }

        return connections;
    }

    public static boolean canConnect (IBlockReader world, BlockPos pos, Direction face) {
        BlockPos adjacent;
        switch (face) {
            default:
            case NORTH: adjacent = pos.north(); break;
            case EAST:  adjacent = pos.east(); break;
            case SOUTH: adjacent = pos.south(); break;
            case WEST:  adjacent = pos.west(); break;
        }
        return world.getBlockState(adjacent).getBlock() instanceof BlockConstructionTape;
    }

    public static boolean canRemoveTStem (IBlockReader world, BlockPos pos, Direction face) {
        BlockState neighbor = world.getBlockState(pos.offset(face));
        switch (face) {
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
