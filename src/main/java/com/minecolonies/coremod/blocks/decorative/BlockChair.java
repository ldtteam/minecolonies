package com.minecolonies.coremod.blocks.decorative;

import java.util.Locale;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecoloniesSeat;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Block that renders as a chair
 *
 */
public class BlockChair extends  AbstractBlockMinecoloniesSeat<BlockChair>
{

    /** Name of the block */
    public static final String BLOCK_PREFIX = "blockchair";

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Light opacity of the block.
     */
    private static final int LIGHT_OPACITY = 255;

    /**
     * The direction the block is facing.
     */
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;

    /**
     *
     */
    public BlockChair()
    {
        super(Material.WOOD);
        init(BLOCK_PREFIX);
    }

    /**
     * @param name name of the localized name of the block.
     */
    public BlockChair(final String name)
    {
        super(Material.WOOD);
        init(name);
    }

    /**
     * Defines all required information to register block to mine craft.
     * @param name name of the localized name of the block.
     */
    private void init(final String name)
    {
        setRegistryName(name);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setTickRandomly(false);
        useNeighborBrightness = true;
        setLightOpacity(LIGHT_OPACITY);
    }


    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    /**
     * @return returns name of the block prefix.
     */
    public String getName()
    {
        return BLOCK_PREFIX;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos pos,
            final EnumFacing side)
    {
        return true;
    }


    @Override
    public boolean canBeConnectedTo(final IBlockAccess world, final BlockPos pos, final EnumFacing facing)
    {
        return false;
    }


    @Override
    public boolean isSeatBeingUsed()
    {
        return false;
    }
}