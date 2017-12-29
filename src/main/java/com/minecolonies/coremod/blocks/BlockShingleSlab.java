package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Decorative block
 */
public class BlockShingleSlab extends AbstractBlockMinecoloniesDirectional<BlockShingleSlab>
{
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    public static final PropertyEnum<ShingleSlabType> VARIANT = PropertyEnum.create("variant", ShingleSlabType.class);

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockShingleSlab";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the TimberFrame
     */
    public BlockShingleSlab()
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
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.ENGLISH), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        this.setLightOpacity(255);
        this.useNeighborBrightness = true;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.HORIZONTALS[meta]);
    }

    @Override
    public AxisAlignedBB getBoundingBox(@NotNull final IBlockState state, @NotNull final IBlockAccess source, @NotNull final BlockPos pos)
    {
        return AABB_BOTTOM_HALF;
    }

    @Override
    public IBlockState getActualState(@NotNull final IBlockState state, @NotNull final IBlockAccess worldIn, @NotNull final BlockPos pos)
    {
        return getSlabShape(state, worldIn, pos);
    }

    /**
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.
     * @return the blockState to use.
     */
    private static IBlockState getSlabShape(@NotNull final IBlockState state, @NotNull final IBlockAccess world, @NotNull final BlockPos position)
    {
        final boolean connectTo1 = !(world.getBlockState(position.east()).getBlock() instanceof BlockShingleSlab);
        final boolean connectTo2 = !(world.getBlockState(position.west()).getBlock() instanceof BlockShingleSlab);
        final boolean connectTo3 = !(world.getBlockState(position.north()).getBlock() instanceof BlockShingleSlab);
        final boolean connectTo4 = !(world.getBlockState(position.south()).getBlock() instanceof BlockShingleSlab);

        int amount = 0;
        if(connectTo1)
        {
            amount++;
        }
        if(connectTo2)
        {
            amount++;
        }
        if(connectTo3)
        {
            amount++;
        }
        if(connectTo4)
        {
            amount++;
        }

        if(amount == 0)
        {
            return state.withProperty(VARIANT, ShingleSlabType.TOP);
        }
        if(amount == 1)
        {
            if (connectTo1)
            {
                return state.withProperty(VARIANT, ShingleSlabType.ONE_WAY).withProperty(FACING, EnumFacing.SOUTH);
            }
            else if (connectTo2)
            {
                return state.withProperty(VARIANT, ShingleSlabType.ONE_WAY).withProperty(FACING, EnumFacing.NORTH);
            }
            else if (connectTo3)
            {
                return state.withProperty(VARIANT, ShingleSlabType.ONE_WAY).withProperty(FACING, EnumFacing.EAST);
            }
            else
            {
                return state.withProperty(VARIANT, ShingleSlabType.ONE_WAY).withProperty(FACING, EnumFacing.WEST);
            }
        }
        else if(amount == 2)
        {
            if (connectTo1 && connectTo2 && !connectTo3 && !connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.EAST);
            }
            else if (!connectTo1 && !connectTo2 && connectTo3 && connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.NORTH);
            }
            else if(!connectTo1 && connectTo2 && connectTo3 && !connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.CURVED).withProperty(FACING, EnumFacing.WEST);
            }
            else if(connectTo1 && !connectTo2 && !connectTo3 && connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.CURVED).withProperty(FACING, EnumFacing.EAST);
            }
            else if(!connectTo1 && connectTo2 && !connectTo3 && connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.CURVED).withProperty(FACING, EnumFacing.SOUTH);
            }
            if(connectTo1 && !connectTo2 && connectTo3 && !connectTo4)
            {
                return state.withProperty(VARIANT, ShingleSlabType.CURVED).withProperty(FACING, EnumFacing.NORTH);
            }
        }
        else if(amount == 3)
        {
            if (!connectTo1)
            {
                if(!world.isAirBlock(position.west().down()))
                {
                    return state.withProperty(VARIANT, ShingleSlabType.THREE_WAY).withProperty(FACING, EnumFacing.NORTH);
                }
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.NORTH);
            }
            else if (!connectTo2)
            {
                if(!world.isAirBlock(position.east().down()))
                {
                    return state.withProperty(VARIANT, ShingleSlabType.THREE_WAY).withProperty(FACING, EnumFacing.SOUTH);
                }
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.SOUTH);
            }
            else if (!connectTo3)
            {
                if(!world.isAirBlock(position.south().down()))
                {
                    return state.withProperty(VARIANT, ShingleSlabType.THREE_WAY).withProperty(FACING, EnumFacing.WEST);
                }
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.WEST);
            }
            else
            {
                if(!world.isAirBlock(position.north().down()))
                {
                    return state.withProperty(VARIANT, ShingleSlabType.THREE_WAY).withProperty(FACING, EnumFacing.EAST);
                }
                return state.withProperty(VARIANT, ShingleSlabType.TWO_WAY).withProperty(FACING, EnumFacing.EAST);
            }
        }
        return state.withProperty(VARIANT, ShingleSlabType.FOUR_WAY);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING, VARIANT});
    }

    @Override
    public boolean isOpaqueCube(@NotNull final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(@NotNull final IBlockState state, @NotNull final IBlockAccess world, @NotNull final BlockPos pos, @NotNull final EnumFacing face)
    {
        return false;
    }
}
