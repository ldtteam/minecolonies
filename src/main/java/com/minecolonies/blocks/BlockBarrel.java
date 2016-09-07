package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class BlockBarrel extends Block
{
    private static final PropertyInteger BARRELSTATE    = PropertyInteger.create("BARRELSTATE", 0, 2);
    private static final PropertyInteger FULLNESS       = PropertyInteger.create("FULLNESS", 0, 10);
    /**
     * The hardness this block has.
     */
    private static final float           BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String          BLOCK_NAME     = "blockBarrel";
    /**
     * The resistance this block has.
     */
    private static final float           RESISTANCE     = 1F;
    private int                          timer          = 0;

    public BlockBarrel()
    {
        super(Material.wood);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FULLNESS, 0).withProperty(BARRELSTATE, 0));

        this.setTickRandomly(true);
        initBlock();
    }

    /**
     * initialize the block
     */
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerBlock(this, BLOCK_NAME);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(FULLNESS, meta & 15).withProperty(BARRELSTATE, (meta & 3) >> 4);
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        int meta = state.getValue(FULLNESS);
        meta |= state.getValue(BARRELSTATE) << 4;
        return meta;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render.
     *
     * @return true
     */
    @Override
    public boolean isOpaqueCube()
    {
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        System.out.println("UpdateTick called");

        int barrelState = state.getValue(BARRELSTATE);
        int fullness = state.getValue(FULLNESS);

        System.out.println("now BARRELSTATE = " + barrelState + " and FULLNESS = " + fullness);

//if statement 2
        if (fullness >= 10 && barrelState == 0)
        {
            System.out.println("if Statement 2 worked");

            worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, 1));
            barrelState = state.getValue(BARRELSTATE);
            System.out.println("New BARRELSTATE = " + barrelState);
        }

        if (barrelState == 1)
        {
            timer++;
            System.out.println("timer ticked");
        }

        if (timer >= 2)
        {
            System.out.println("timer reached " + timer);

            fullness = 0;
            barrelState = 2;
            timer = 0;
            worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, barrelState));
            worldIn.setBlockState(pos, state.withProperty(FULLNESS, fullness));

            System.out.println("new BARRELSTATE = " + barrelState + " And FULLNESS = " + fullness);
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        System.out.println("block right-clicked");

        ItemStack itemstack = playerIn.inventory.getCurrentItem();
        UseBarrel(worldIn, playerIn, itemstack, state, pos);
        return true;
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, BARRELSTATE, FULLNESS);
    }

    //whenever player right click to barrel call this.
    public boolean UseBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemstack, IBlockState state, BlockPos pos)
    {
        System.out.println("block activated");

        int barrelState = state.getValue(BARRELSTATE);
        int fullness = state.getValue(FULLNESS);

        System.out.println("At this moment bs= " + barrelState + " and fl=" + fullness);


        //if statement 1
        if (barrelState == 2)
        {
            System.out.println("because of bs=" + barrelState + " if statement 1 worked");

            playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 8));
            worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, 0));
            barrelState = state.getValue(BARRELSTATE);
            System.out.println("now BARRELSTATE = " + barrelState);
            return true;
        }

        if (itemstack == null)
        {
            return true;
        }

        Item item = itemstack.getItem();

        if (item == Items.rotten_flesh && barrelState == 0 && fullness < 10)
        {
            System.out.println("item Consumed");

            itemstack.stackSize--;
            fullness += 1;
            if (fullness > 10)
            {
                fullness = 10;
            }
            worldIn.setBlockState(pos, state.withProperty(FULLNESS, fullness));
            System.out.println("now FULLNESS = " + fullness);

            return true;
        }

        return true;
    }

    public void AddItemToBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemStack, IBlockState state, BlockPos pos)
    {
        UseBarrel(worldIn, playerIn, itemStack, state, pos);
    }

    public void GetItemFromBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemStack, IBlockState state, BlockPos pos)
    {
        final int bs = state.getValue(BARRELSTATE);
        if (bs == 2)
        {
            UseBarrel(worldIn, playerIn, null, state, pos);
        }
    }
}
