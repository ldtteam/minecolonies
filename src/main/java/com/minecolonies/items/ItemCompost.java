package com.minecolonies.items;

/**
 * Created by Northstar on 8/29/2016.
 */

import com.minecolonies.creativetab.ModCreativeTabs;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCompost extends AbstractItemMinecolonies
{
    /**
     * Compost constructor, Set max stack size to 64 like all other items.
     */
    public ItemCompost()
    {
        super("compost");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = 64;
    }

    //whenever player right click a block with this "compost item", call this method
    public boolean onItemUse(
                              final ItemStack stack,
                              final EntityPlayer playerIn,
                              final World worldIn,
                              final BlockPos pos,
                              final EnumFacing side,
                              final float hitX,
                              final float hitY,
                              final float hitZ)
    {

        final IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() instanceof IGrowable)
        {
            final IGrowable igrowable = (IGrowable) iblockstate.getBlock();

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
}


