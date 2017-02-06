package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.EnumFacing.*;

/**
 * This block is used as a border to show the size of the building.
 * It also shows that the building is in the progress of being built.
 */
public class BlockConstructionTape extends Block
{

    /**
     * The position it faces.
     */
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 50.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockConstructionTape";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 2000.0F;

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
    private static final double SN_START_COLLISION_Z = 0.4375;

    /**
     * End of the collision box facing South/North.
     */
    private static final double SN_END_COLLISION_Z = 0.5625;
    /**
     * Start of the collision box at x facing South/North.
     */
    private static final double WE_START_COLLISION_X = 0.4375;

    /**
     * End of the collision box facing South/North.
     */
    private static final double WE_END_COLLISION_X = 0.5625;

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
     * How much light goes through the block.
     */
    private static final int LIGHT_OPACITY = 0;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTape()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.register(this);
        GameRegistry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
        setBlockUnbreakable();
    }

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    /**
     * Convert the given metadata into a BlockState for this Block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(final int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SideOnly(Side.CLIENT)
    @Override
    @Deprecated
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @Override
    @Deprecated
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
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
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos)
    {

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

    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================
    @Override
    public IBlockState onBlockPlaced(
            final World worldIn,
            final BlockPos pos,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ,
            final int meta,
            @Nullable final EntityLivingBase placer)
    {
        @NotNull final EnumFacing enumFacing = (placer == null) ? NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }
}
