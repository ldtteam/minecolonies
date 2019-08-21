package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

/**
 * Class used to handle the compost item.
 */
public class ItemCompost extends AbstractItemMinecolonies
{

    /***
     * Constructor for the ItemCompost
     */
    public ItemCompost()
    {
        super("compost");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = Constants.STACKSIZE;
    }

    /***
     * Called whenever the player uses the item
     * @param player the player that is using the item
     * @param worldIn the world
     * @param pos the position
     * @param hand the hand of hte player (will contain a stack of ItemCompost)
     * @param facing the direction the player is facing
     * @param hitX the X coord to where the player used the item
     * @param hitY the Y coord to where the player used the item
     * @param hitZ the Z coord to where the player used the item
     * @return EnumActionResult.SUCCESS if it could apply the event, EnumActionResult.FAIL if not
     */
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World worldIn, final BlockPos pos, final EnumHand hand,
                                      final EnumFacing facing, final float hitX, final float hitY, final float hitZ)
    {
        final ItemStack itemstack = player.getHeldItem(hand);
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

    /***
     * If the target is a IGrowable, it makes it grow
     * @param stack the stack of items that is used to apply the bone meal event (ItemCompost)
     * @param worldIn the world
     * @param target the target for the bone meal event
     * @param player the player using triggering the event
     * @param hand the hand of the player
     * @return true if it could apply the event, false if not
     */
    public static boolean applyBonemeal(final ItemStack stack, final World worldIn, final BlockPos target, final EntityPlayer player,
                                        @Nullable final EnumHand hand)
    {
        final IBlockState iblockstate = worldIn.getBlockState(target);
        final int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, iblockstate, stack, hand);
        if (hook != 0)
        {
            return hook > 0;
        }
        else
            {
            if (iblockstate.getBlock() instanceof IGrowable)
            {
                final IGrowable igrowable = (IGrowable)iblockstate.getBlock();
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


