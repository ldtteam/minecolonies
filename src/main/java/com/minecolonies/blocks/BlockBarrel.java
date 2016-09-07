package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import java.util.Random;

/**
 * Created by Northstar on 8/29/2016.
 */

public class BlockBarrel extends Block
{
    public int timer=0;

    public int BarrelState=0;
    //barrel's fullnes state
    private  int fullnes =0;
    //is compost cooked ?
    private  boolean isCompostReady =false;
    /**
     * The hardness this block has.
     */
    private static final float  BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME     = "blockBarrel";

    /**
     * The resistance this block has.
     */
    private static final float  RESISTANCE     = 1F;


    public BlockBarrel()
    {
        super(Material.wood);
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

    //whenever player right click to barrel call this.
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        System.out.println("activated block");
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {

            if(isCompostReady==true&&BarrelState==2)
            {
                playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost,8));
                isCompostReady = false;
                BarrelState=0;
                return true;
            }

            //check what current item player has.
            ItemStack itemstack = playerIn.inventory.getCurrentItem();

            if (itemstack == null)
            {
                return true;
            }
            Item item = itemstack.getItem();

            if (item== Items.spider_eye && fullnes<100 && BarrelState==0) { itemstack.stackSize--; fullnes+=10; System.out.println("item consumed, new fullness: " + fullnes); }
            if (item== Items.fish && fullnes<100 && BarrelState==0) { itemstack.stackSize--; fullnes+=5; System.out.println("item consumed, new fullness: " + fullnes); }
            if (item== Items.rotten_flesh && fullnes<100 && BarrelState==0) { itemstack.stackSize--; fullnes+=15; System.out.println("item consumed, new fullness: " + fullnes); }

            if (fullnes>=100&&BarrelState==0)
            {
                BarrelState=1;
                System.out.println("fullnes reached " +fullnes + "and Barrel State changed to " + BarrelState);
            }
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        System.out.println("Update method being called");

        if (BarrelState==1)
        {
            timer++;
            System.out.println("timer start ticking: "+timer);
        }
        if (timer>=2)
        {
            fullnes=0;
            BarrelState=2;
            isCompostReady=true;
            timer=0;
            System.out.println("timer reached to"+timer+" , set fullnes=0, curr. fullness"+fullnes+" and Barrel State=" + BarrelState);
        }
    }
}
