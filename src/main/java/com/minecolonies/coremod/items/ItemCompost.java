package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.IGrowable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
     * @return ActionResultType.SUCCESS if it could apply the event, ActionResultType.FAIL if not
     */
    @Override
    public ActionResultType onItemUse(final PlayerEntity player, final World worldIn, final BlockPos pos, final Hand hand,
                                      final Direction facing, final float hitX, final float hitY, final float hitZ)
    {
        final ItemStack itemstack = player.getHeldItem(hand);
        if (applyBonemeal(itemstack, worldIn, pos, player, hand))
        {
            if (!worldIn.isRemote)
            {
                worldIn.playEvent(2005, pos, 0);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
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
    public static boolean applyBonemeal(final ItemStack stack, final World worldIn, final BlockPos target, final PlayerEntity player,
                                        @Nullable final Hand hand)
    {
        final BlockState BlockState = worldIn.getBlockState(target);
        final int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, BlockState, stack, hand);
        if (hook != 0)
        {
            return hook > 0;
        }
        else
            {
            if (BlockState.getBlock() instanceof IGrowable)
            {
                final IGrowable igrowable = (IGrowable)BlockState.getBlock();
                if (igrowable.canGrow(worldIn, target, BlockState, worldIn.isRemote))
                {
                    if (!worldIn.isRemote)
                    {
                        if (igrowable.canUseBonemeal(worldIn, worldIn.rand, target, BlockState))
                        {
                            igrowable.grow(worldIn, worldIn.rand, target, BlockState);
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


