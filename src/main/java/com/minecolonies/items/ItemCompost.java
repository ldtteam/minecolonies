package com.minecolonies.items;

/**
 * Created by Northstar on 8/29/2016.
 */

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;



public class ItemCompost extends AbstractItemMinecolonies
{
    //max stack size = 64.
    public ItemCompost()
    {
        super();
        maxStackSize = 64;
    }

    //whenever player right click a block with this "compost item", call this method
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {

        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() instanceof IGrowable)
        {
            IGrowable igrowable = (IGrowable) iblockstate.getBlock();

            if (igrowable.canGrow(worldIn, pos, iblockstate, worldIn.isRemote))
            {
                if (!worldIn.isRemote)
                {
                    if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, iblockstate))
                    {
                        igrowable.grow(worldIn, worldIn.rand, pos, iblockstate);
                    }

                    --stack.stackSize;
                }
            }
        }
        return true;
    }




    @Override
    public String getName()
    {
        return "compost";
    }
}


