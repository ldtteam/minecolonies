package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
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
    public InteractionResult useOn(final UseOnContext ctx)
    {
        final ItemStack itemstack = ctx.getPlayer().getItemInHand(ctx.getHand());
        if (applyBonemeal(itemstack, ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer()))
        {
            if (!ctx.getLevel().isClientSide)
            {
                ctx.getLevel().levelEvent(2005, ctx.getClickedPos(), 0);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static boolean applyBonemeal(final ItemStack stack, final Level worldIn, final BlockPos target, final Player player)
    {
        final BlockState BlockState = worldIn.getBlockState(target);
        final int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, BlockState, stack);
        if (hook != 0)
        {
            return hook > 0;
        }
        else
        {
            if (BlockState.getBlock() instanceof BonemealableBlock)
            {
                final BonemealableBlock igrowable = (BonemealableBlock) BlockState.getBlock();
                if (igrowable.isValidBonemealTarget(worldIn, target, BlockState, worldIn.isClientSide))
                {
                    if (!worldIn.isClientSide)
                    {
                        if (igrowable.isBonemealSuccess(worldIn, worldIn.random, target, BlockState))
                        {
                            igrowable.performBonemeal((ServerLevel) worldIn, worldIn.random, target, BlockState);
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


