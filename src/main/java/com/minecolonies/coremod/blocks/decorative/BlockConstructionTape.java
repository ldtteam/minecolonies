package com.minecolonies.coremod.blocks.decorative;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
import static net.minecraft.util.EnumFacing.*;

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
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, NORTH));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
    }

    /**
     * Convert the given metadata into a BlockState for this Block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(final int meta)
    {
        EnumFacing enumfacing = byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return true;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(final IBlockState stateIn, final IBlockAccess source, final BlockPos pos)
    {
        final IBlockState state = getActualState(stateIn, source, pos);
        if(state.getValue(VARIANT).equals(AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER))
        {
            if (state.getValue(FACING).equals(NORTH))
            {
                return new AxisAlignedBB((float) N_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) N_START_COLLISION_Z,
                        (float) N_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) N_END_COLLISION_Z);
            }
            if (state.getValue(FACING).equals(WEST))
            {
                return new AxisAlignedBB((float) W_START_COLLISION_X,
                        (float) BOTTOM_COLLISION,
                        (float) W_START_COLLISION_Z,
                        (float) W_END_COLLISION_X,
                        (float) HEIGHT_COLLISION,
                        (float) W_END_COLLISION_Z);
            }
            if (state.getValue(FACING).equals(SOUTH))
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

        if (state.getValue(FACING).equals(EAST) || state.getValue(FACING).equals(WEST))
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
    public IBlockState getActualState(@NotNull final IBlockState state, @NotNull final IBlockAccess worldIn, @NotNull final BlockPos pos)
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
    private static IBlockState getTapeShape(@NotNull final IBlockState state, @NotNull final IBlockAccess world, @NotNull final BlockPos position)
    {
        final boolean[] connectors = new boolean[]{world.getBlockState(position.east()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.west()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.north()).getBlock() instanceof BlockConstructionTape,
                world.getBlockState(position.south()).getBlock() instanceof BlockConstructionTape};

        if((connectors[0] && connectors[2]) || (connectors[0] && connectors[3]) || (connectors[1] && connectors[3]) || (connectors[1] && connectors[2]))
        {
            return state.withProperty(VARIANT, AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.CORNER);
        }
        return state.withProperty(VARIANT, AbstractBlockMinecoloniesConstructionTape.ConstructionTapeType.STRAIGHT);
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @SideOnly(Side.CLIENT)
    @Override
    @Deprecated
    public boolean shouldSideBeRendered(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side)
    {
        return true;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Nullable
    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos)
    {
        return null;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @Nullable
    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune)
    {
        return null;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    @Deprecated
    public IBlockState getStateForPlacement(
                                             final World worldIn,
                                             final BlockPos pos,
                                             final EnumFacing facing,
                                             final float hitX,
                                             final float hitY,
                                             final float hitZ,
                                             final int meta,
                                             final EntityLivingBase placer)
    {
        @NotNull final EnumFacing enumFacing = (placer == null) ? NORTH : fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING, VARIANT});
    }
}
