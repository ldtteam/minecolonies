package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
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
     * How much light goes through the block.
     */
    private static final int    LIGHT_OPACITY    = 0;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTape()
    {
        super(Material.VINE);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.ENGLISH), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        this.setDefaultState(this.blockState.getBaseState().with(FACING, NORTH));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
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

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(final BlockState stateIn, final IBlockAccess source, final BlockPos pos)
    {
        final BlockState state = getActualState(stateIn, source, pos);
        if(state.get(VARIANT).equals(AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER))
        {
            if (state.get(FACING).equals(NORTH))
            {
                return new AxisAlignedBB((float) N_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) N_START_COLLISION_Z,
                        (float) N_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) N_END_COLLISION_Z);
            }
            if (state.get(FACING).equals(WEST))
            {
                return new AxisAlignedBB((float) W_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) W_START_COLLISION_Z,
                        (float) W_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) W_END_COLLISION_Z);
            }
            if (state.get(FACING).equals(SOUTH))
            {
                return new AxisAlignedBB((float) S_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) S_START_COLLISION_Z,
                        (float) S_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) S_END_COLLISION_Z);
            }
            else
            {
                return new AxisAlignedBB((float) E_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) E_START_COLLISION_Z,
                        (float) E_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) E_END_COLLISION_Z);
            }
        }

        if (state.get(FACING).equals(EAST) || state.get(FACING).equals(WEST))
        {
            return new AxisAlignedBB((float) WE_START_COLLISION_X,
                                      (float) BOTTOM_COLLISION,
                                      (float) WE_START_COLLISION_Z,
                                      (float) WE_END_COLLISION_X,
                                      (float) HEIGHT_COLLISION,
                                      (float) WE_END_COLLISION_Z);
        }
        else
        {
            return new AxisAlignedBB((float) SN_START_COLLISION_X,
                                      (float) BOTTOM_COLLISION,
                                      (float) SN_START_COLLISION_Z,
                                      (float) SN_END_COLLISION_X,
                                      (float) HEIGHT_COLLISION,
                                      (float) SN_END_COLLISION_Z);
        }
    }

    /**
     * @deprecated remove when minecraft invents something better.
     */
    @Deprecated
    @Override
    public BlockState getActualState(@NotNull final BlockState state, @NotNull final IBlockAccess worldIn, @NotNull final BlockPos pos)
    {
        return getTapeShape(state, worldIn, pos);
    }

    /**
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
    private static BlockState getTapeShape(@NotNull final BlockState state, @NotNull final IBlockAccess world, @NotNull final BlockPos position)
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

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Nullable
    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final BlockState blockState, final IBlockAccess worldIn, final BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        TallGrassBlock
        @NotNull final Direction Direction = (context.getPlayer() == null) ? NORTH : fromAngle(context.getPlayer().rotationYaw);
        return this.getDefaultState().with(FACING, Direction);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }
}
