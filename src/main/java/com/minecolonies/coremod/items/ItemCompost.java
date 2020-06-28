package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Class used to handle the compost item.
 */
public class ItemCompost extends AbstractItemMinecolonies
{

    /***
     * Constructor for the ItemCompost
     * @param properties the properties.
     */
    public ItemCompost(final Item.Properties properties)
    {
        super("compost", properties.maxStackSize(Constants.STACKSIZE).group(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext ctx)
    {
        final ItemStack itemstack = ctx.getPlayer().getHeldItem(ctx.getHand());
        if (applyBonemeal(itemstack, ctx.getWorld(), ctx.getPos(), ctx.getPlayer()))
        {
            if (!ctx.getWorld().isRemote)
            {
                ctx.getWorld().playEvent(2005, ctx.getPos(), 0);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }


    public static boolean applyBonemeal(final ItemStack stack, final World worldIn, final BlockPos target, final PlayerEntity player)
    {
        final BlockState BlockState = worldIn.getBlockState(target);
        final int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, BlockState, stack);
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
                            igrowable.grow((ServerWorld) worldIn, worldIn.rand, target, BlockState);
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


