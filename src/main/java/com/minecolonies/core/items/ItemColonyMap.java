package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.core.client.gui.map.WindowColonyMap;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_MAP_COLONY_SET;

/**
 * Class describing the colonymap item.
 */
public class ItemColonyMap extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the colonymap item.
     *
     * @param properties the properties.
     */
    public ItemColonyMap(final Item.Properties properties)
    {
        super("colonymap", properties.stacksTo(1));
    }

    @Override
    @NotNull
    public InteractionResult useOn(final UseOnContext ctx)
    {
        final ItemStack colonymap = ctx.getPlayer().getItemInHand(ctx.getHand());

        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof TileEntityColonyBuilding buildingEntity)
        {
            buildingEntity.writeColonyToItemStack(colonymap);

            if (!ctx.getLevel().isClientSide)
            {
                MessageUtils.format(COM_MINECOLONIES_MAP_COLONY_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(colonymap, ctx.getLevel(), ctx.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(
        final Level worldIn,
        final Player playerIn,
        final InteractionHand hand)
    {
        final ItemStack colonymap = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, colonymap);
        }

        openWindow(colonymap, worldIn, playerIn);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, colonymap);
    }

    /**
     * Opens the colonymap window if there is a valid colony linked
     * @param stack the item
     * @param player the player entity opening the window
     */
    private static void openWindow(ItemStack stack, Level world, Player player)
    {
        final IColonyView colonyView = ColonyId.readColonyViewFromItemStack(stack);
        if (colonyView != null && colonyView.getTownHall() != null)
        {
            new WindowColonyMap(false, colonyView.getTownHall()).open();
        }
        else
        {
            player.displayClientMessage(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_MAP_NEED_COLONY), true);
        }
    }
}
