package com.minecolonies.coremod.items;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;

import javax.annotation.Nullable;

/**
 * Class used to handle the compost item.
 */
public class ItemCompost extends AbstractItemMinecolonies
{
    private static final int MAX_STACK_SIZE = 64;

    public ItemCompost()
    {
        super("compost");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = MAX_STACK_SIZE;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if (applyBonemeal(itemstack, worldIn, pos, player, hand))
        {
            if (!worldIn.isRemote)
            {
                worldIn.playEvent(2005, pos, 0);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public static boolean applyBonemeal(ItemStack stack, World worldIn, BlockPos target, EntityPlayer player,
                                        @Nullable EnumHand hand)
    {
        IBlockState iblockstate = worldIn.getBlockState(target);
        int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, iblockstate, stack, hand);
        if (hook != 0)
        {
            return hook > 0;
        }
        else
            {
            if (iblockstate.getBlock() instanceof IGrowable)
            {
                IGrowable igrowable = (IGrowable)iblockstate.getBlock();
                if (igrowable.canGrow(worldIn, target, iblockstate, worldIn.isRemote))
                {
                    if (!worldIn.isRemote)
                    {
                        if (igrowable.canUseBonemeal(worldIn, worldIn.rand, target, iblockstate))
                        {
                            igrowable.grow(worldIn, worldIn.rand, target, iblockstate);
                        }

                        stack.shrink(1);
                    }
                    return true;
                }
            }
            return false;
        }
    }

}


