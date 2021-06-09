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
        super("compost", properties.stacksTo(Constants.STACKSIZE).tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    public ActionResultType useOn(final ItemUseContext ctx)
    {
        final ItemStack itemstack = ctx.getPlayer().getLastHandItem(ctx.getHand());
        if (applyBonemeal(itemstack, ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer()))
        {
            if (!ctx.getLevel().isClientSide)
            {
                ctx.getLevel().levelEvent(2005, ctx.getClickedPos(), 0);
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
                final IGrowable igrowable = (IGrowable) BlockState.getBlock();
                if (igrowable.isValidBonemealTarget(worldIn, target, BlockState, worldIn.isClientSide))
                {
                    if (!worldIn.isClientSide)
                    {
                        if (igrowable.isBonemealSuccess(worldIn, worldIn.random, target, BlockState))
                        {
                            igrowable.performBonemeal((ServerWorld) worldIn, worldIn.random, target, BlockState);
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


