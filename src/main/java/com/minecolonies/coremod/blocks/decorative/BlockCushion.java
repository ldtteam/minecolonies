package com.minecolonies.coremod.blocks.decorative;

import java.util.Locale;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecoloniesSeat;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Block that renders as a tiny "cushion"
 *
 */
public class BlockCushion extends  AbstractBlockMinecoloniesSeat<BlockCushion>
{
    /** Name of the block */
    public static final String BLOCK_PREFIX = "blockcushion";

    /**
     * The direction the block is facing.
     */
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;

    /**
     *
     */
    public BlockCushion()
    {
        super(Material.CARPET);
        init(BLOCK_PREFIX);
    }

    /**
     * @param name name of the localized block name
     */
    public BlockCushion(final String name)
    {
        super(Material.CARPET);
        init(name);
    }

    /**
     * Defines all required information to register block to mine craft.
     * @param name name of the localized name of the block.
     */
   private void init(final String name)
    {
        setRegistryName(name);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(0.5F);
        setSoundType(SoundType.CLOTH);
        setTickRandomly(false);
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
        return side == EnumFacing.UP ? true : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public boolean isSeatBeingUsed()
    {
        return false;
    }

}