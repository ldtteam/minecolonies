package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

public class BlockBarrel extends AbstractBlockMinecolonies<BlockBarrel>
{

    //public static final PropertyEnum<BarrelBlockType> VARIANT        = PropertyEnum.create("variant", BarrelBlockType.class);
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String                     BLOCK_NAME     = "Crate";
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    public BlockBarrel()
    {
        super(Material.WOOD);
        //this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BarrelBlockType.ZERO));
        initBlock();
    }

    //todo: register block with new method

    /**
     * initialize the block
     */
    @SuppressWarnings(DEPRECATION)
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }



    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks
     * for render.
     *
     * @return true
     */
    //todo: remove once we no longer need to support this
    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return true;
    }

    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityBarrel)
        {
            ((TileEntityBarrel) te).updateTick(worldIn, pos, state, rand);
        }
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state)
    {
        return new TileEntityBarrel();
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(
            final World worldIn,
            final BlockPos pos,
            final IBlockState state,
            final EntityPlayer playerIn,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        Log.getLogger().info("block right-clicked");

        final ItemStack itemstack = playerIn.inventory.getCurrentItem();
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityBarrel)
        {
            ((TileEntityBarrel) te).useBarrel(worldIn, playerIn, itemstack, state, pos);
        }
        return true;
    }
}
