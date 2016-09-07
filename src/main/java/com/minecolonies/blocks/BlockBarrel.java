package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
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

public class BlockBarrel extends Block
{
    public static final PropertyInteger BARRELSTATE = PropertyInteger.create("BARRELSATE",0,2);
    public static final PropertyInteger FULLNES = PropertyInteger.create("FULLNES",0,10);
    private int bs=0;
    private int fl=0;



    private int timer=0;

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
        this.setDefaultState(this.blockState.getBaseState().withProperty(FULLNES, Integer.valueOf(0)).withProperty(BARRELSTATE,Integer.valueOf(0)));

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

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        System.out.println("block right-clicked");

        ItemStack itemstack = playerIn.inventory.getCurrentItem();
        UseBarrel(worldIn,playerIn,itemstack,state,pos);
       return true;
    }

    public void AddItemToBarrel(World worldIn,EntityPlayer playerIn,ItemStack itemStack,IBlockState state,BlockPos pos)
    {
        UseBarrel(worldIn,playerIn,itemStack,state,pos);
    }
    public void GetItemFromBarrel(World worldIn,EntityPlayer playerIn,ItemStack itemStack,IBlockState state,BlockPos pos)
    {
        bs = ((Integer)state.getValue(BARRELSTATE)).intValue();
        if(bs==2)
        {
            UseBarrel(worldIn,playerIn,null,state,pos);
        }
    }

    //whenever player right click to barrel call this.
    public boolean UseBarrel(World worldIn,EntityPlayer playerIn, ItemStack itemstack,IBlockState state,BlockPos pos)
    {
        System.out.println("block activated");

        bs = ((Integer)state.getValue(BARRELSTATE)).intValue();
        fl = ((Integer)state.getValue(FULLNES)).intValue();

        System.out.println("At this moment bs= "+bs+" and fl="+fl);


        //if statement 1
        if(bs==2)
            {
                System.out.println("because of bs="+bs+" if statement 1 worked");

                playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost,8));
                worldIn.setBlockState(pos,state.withProperty(BARRELSTATE, Integer.valueOf(0)));
                bs = ((Integer)state.getValue(BARRELSTATE)).intValue();
                System.out.println("now BARRELSTATE = "+bs);
                return true;
            }

            if (itemstack == null) {return true;}

            Item item = itemstack.getItem();

            if (item==Items.rotten_flesh&&bs==0&&fl<10)
            {
                System.out.println("item Consumed");

                itemstack.stackSize--;
                fl+=1;
                if (fl>10){fl=10;}
                worldIn.setBlockState(pos,state.withProperty(FULLNES, Integer.valueOf(fl)));
                System.out.println("now FULLNES = "+fl);

                return  true;
            }

        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        System.out.println("UpdateTick called");

        bs = ((Integer)state.getValue(BARRELSTATE)).intValue();
        fl = ((Integer)state.getValue(FULLNES)).intValue();

        System.out.println("now BARRELSTATE = "+bs+ " and FULLNES = "+fl);

//if statement 2
        if (fl>=10&&bs==0)
        {
            System.out.println("if Statement 2 worked");

            worldIn.setBlockState(pos,state.withProperty(BARRELSTATE, Integer.valueOf(1)));
            bs = ((Integer)state.getValue(BARRELSTATE)).intValue();
            System.out.println("New BARRELSTATE = "+bs);


        }

        if (bs==1) {timer++; System.out.println("timer ticked");}

        if (timer>=2)
        {
            System.out.println("timer reached " +timer);

            fl=0;
            bs=2;
            timer=0;
            worldIn.setBlockState(pos,state.withProperty(BARRELSTATE, Integer.valueOf(bs)));
            worldIn.setBlockState(pos,state.withProperty(FULLNES, Integer.valueOf(fl)));

            System.out.println("new BARRELSTATE = "+bs+" And FULLNES = " +fl);


        }
    }

}
