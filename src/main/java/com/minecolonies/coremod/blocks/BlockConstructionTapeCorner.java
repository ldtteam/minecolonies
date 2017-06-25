package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
import static net.minecraft.util.EnumFacing.*;

/**
 * This Block has the same purpose as the BlockConstructionTape.
 * The only difference is: it's a corner piece.
 */
public class BlockConstructionTapeCorner extends Block
{

    /**
     * The position it faces.
     */
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockConstructionTapeCorner";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 0.0F;

    /**
     * Start of the collision box at y.
     */
    private static final double BOTTOM_COLLISION = 0.0;

    /**
     * Start of the collision box at x facing North.
     */
    private static final double N_START_COLLISION_X = 0.0;

    /**
     * End of the collision box facing North.
     */
    private static final double N_END_COLLISION_X = 0.5625;

    /**
     * Start of the collision box at z facing North.
     */
    private static final double N_START_COLLISION_Z = 0.0;

    /**
     * End of the collision box facing North.
     */
    private static final double N_END_COLLISION_Z = 0.5625;

    /**
     * Start of the collision box at x facing West.
     */
    private static final double W_START_COLLISION_X = 0.0;

    /**
     * End of the collision box facing West.
     */
    private static final double W_END_COLLISION_X = 0.5625;

    /**
     * Start of the collision box at z facing West.
     */
    private static final double W_START_COLLISION_Z = 0.4375;

    /**
     * End of the collision box facing West.
     */
    private static final double W_END_COLLISION_Z = 1.0;

    /**
     * Start of the collision box at x facing South.
     */
    private static final double S_START_COLLISION_X = 0.4375;

    /**
     * End of the collision box facing South.
     */
    private static final double S_END_COLLISION_X = 1.0;

    /**
     * Start of the collision box at z facing South.
     */
    private static final double S_START_COLLISION_Z = 0.4375;

    /**
     * End of the collision box facing South.
     */
    private static final double S_END_COLLISION_Z = 1.0;

    /**
     * Start of the collision box at x facing East.
     */
    private static final double E_START_COLLISION_X = 0.4375;

    /**
     * End of the collision box facing East.
     */
    private static final double E_END_COLLISION_X = 1.0;

    /**
     * Start of the collision box at z facing East.
     */
    private static final double E_START_COLLISION_Z = 0.0;

    /**
     * End of the collision box facing East.
     */
    private static final double E_END_COLLISION_Z = 0.5625;

    /**
     * Height of the collision box.
     */
    private static final double HEIGHT_COLLISION = 1.0;
    /**
     * How much light goes through the block.
     */
    private static final int    LIGHT_OPACITY    = 0;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockConstructionTapeCorner()
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
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
    }

    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     * @return the block itself.
     */
    public BlockConstructionTapeCorner registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return this;
    }

    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     * @return the block itself.
     */
    public Block registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
        return this;
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

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getIndex();
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

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
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
    @javax.annotation.Nullable
    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos)
    {
        return null;
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

    @Nullable
    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune)
    {
        return null;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
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
        @NotNull final EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }
}
