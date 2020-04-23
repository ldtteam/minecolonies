package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTape()
    {
        super(Properties.create(Material.TALL_PLANTS).hardnessAndResistance(0.0f).doesNotBlockMovement().noDrops());
        setRegistryName(BLOCK_NAME);
        this.setDefaultState(this.getDefaultState().with(FACING, NORTH));
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        if(state.get(VARIANT).equals(AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER))
        {
            if (state.get(FACING).equals(NORTH))
            {
                return SHAPE_NORTH;
            }
            if (state.get(FACING).equals(WEST))
            {
                return SHAPE_WEST;
            }
            if (state.get(FACING).equals(SOUTH))
            {
                return SHAPE_SOUTH;
            }

            return SHAPE_EAST;
        }

        if (state.get(FACING).equals(EAST) || state.get(FACING).equals(WEST))
        {
            return EAST_WEST;
        }
        else
        {
            return NORTH_SOUTH;
        }
    }

    @NotNull
    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction dir, final BlockState state, final IWorld worldIn, @NotNull final BlockPos currentPos, final BlockPos pos)
    {
        super.updatePostPlacement(stateIn, dir, state, worldIn, currentPos, pos);
        return getOptimalStateForPlacement(stateIn, worldIn, currentPos);
    }

    /**
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
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

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return getOptimalStateForPlacement(getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()), context.getWorld(), context.getPos());
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, VARIANT);
    }
}
