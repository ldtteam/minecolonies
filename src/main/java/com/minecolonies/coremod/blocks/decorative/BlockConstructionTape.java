package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
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
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockconstructiontape";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 0.0F;

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

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTape()
    {
        super(Properties.create(Material.ORGANIC).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE).doesNotBlockMovement());
        setRegistryName(BLOCK_NAME);
        this.setDefaultState(this.getDefaultState().with(FACING, NORTH));
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        if(state.get(VARIANT).equals(AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER))
        {
            if (state.get(FACING).equals(NORTH))
            {
                return Block.makeCuboidShape((float) N_START_COLLISION_X,
                  (float) BOTTOM_COLLISION,
                  (float) N_START_COLLISION_Z,
                  (float) N_END_COLLISION_X,
                  (float) HEIGHT_COLLISION,
                  (float) N_END_COLLISION_Z);
            }
            if (state.get(FACING).equals(WEST))
            {
                return Block.makeCuboidShape((float) W_START_COLLISION_X,
                  (float) BOTTOM_COLLISION,
                  (float) W_START_COLLISION_Z,
                  (float) W_END_COLLISION_X,
                  (float) HEIGHT_COLLISION,
                  (float) W_END_COLLISION_Z);
            }
            if (state.get(FACING).equals(SOUTH))
            {
                return Block.makeCuboidShape((float) S_START_COLLISION_X,
                  (float) BOTTOM_COLLISION,
                  (float) S_START_COLLISION_Z,
                  (float) S_END_COLLISION_X,
                  (float) HEIGHT_COLLISION,
                  (float) S_END_COLLISION_Z);
            }
            else
            {
                return Block.makeCuboidShape((float) E_START_COLLISION_X,
                  (float) BOTTOM_COLLISION,
                  (float) E_START_COLLISION_Z,
                  (float) E_END_COLLISION_X,
                  (float) HEIGHT_COLLISION,
                  (float) E_END_COLLISION_Z);
            }
        }

        if (state.get(FACING).equals(EAST) || state.get(FACING).equals(WEST))
        {
            return Block.makeCuboidShape((float) WE_START_COLLISION_X,
              (float) BOTTOM_COLLISION,
              (float) WE_START_COLLISION_Z,
              (float) WE_END_COLLISION_X,
              (float) HEIGHT_COLLISION,
              (float) WE_END_COLLISION_Z);
        }
        else
        {
            return Block.makeCuboidShape((float) SN_START_COLLISION_X,
              (float) BOTTOM_COLLISION,
              (float) SN_START_COLLISION_Z,
              (float) SN_END_COLLISION_X,
              (float) HEIGHT_COLLISION,
              (float) SN_END_COLLISION_Z);
        }
    }

    @NotNull
    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction HORIZONTAL_FACING, final BlockState HORIZONTAL_FACINGState, final IWorld worldIn, @NotNull final BlockPos currentPos, final BlockPos HORIZONTAL_FACINGPos)
    {
        return getTapeShape(stateIn, worldIn, currentPos);
    }

    /**
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
    private static BlockState getTapeShape(@NotNull final BlockState state, @NotNull final IWorld world, @NotNull final BlockPos position)
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
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return getTapeShape(getDefaultState(), context.getWorld(), context.getPos());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }
}
