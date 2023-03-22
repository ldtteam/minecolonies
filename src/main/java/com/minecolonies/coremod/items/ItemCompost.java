package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import static net.minecraft.world.item.BoneMealItem.applyBonemeal;

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
}


