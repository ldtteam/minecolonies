package com.minecolonies.blocks;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/**
 * Created by Northstar on 8/29/2016.
 */

public class BlockBarrel extends Block
{

    //barrel's fullnes state
    private  int fullnes =0;
    //is compost cooked ?
    private  boolean isCompostReady =true;
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
        //this line doens't work . i guess i need to register render. TURN BACK HERE WHEN EVERYTHING ELSE OK.
        super(Material.wood);
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
        if (worldIn.isRemote)
        {
            return true;
        }
        else
            {
                //check what current item player has.
                ItemStack itemstack = playerIn.inventory.getCurrentItem();
                Item item = itemstack.getItem();

                if(isCompostReady==true)
                {
                   playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost,8));
                    isCompostReady = false;
                }

            else if (item== Items.rotten_flesh && fullnes<100)
            {
                itemstack.stackSize--;
                fullnes+=10;

                return true;
            }


            if (fullnes>=100)
            {
                fullnes=0;
                isCompostReady=true;
            }

        }

        return true;
    }


}
