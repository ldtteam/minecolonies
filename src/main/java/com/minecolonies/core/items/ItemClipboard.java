package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.core.client.gui.WindowClipBoard;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET;

/**
 * Class describing the clipboard item.
 */
public class ItemClipboard extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    public static final String TAG_COLONY = "colony";

    /**
     * Tag of the "hide unimportant" UI toggle.
     */
    public static final String TAG_HIDEUNIMPORTANT = "hideunimportant";

    /**
     * Sets the name, creative tab, and registers the Clipboard item.
     *
     * @param properties the properties.
     */
    public ItemClipboard(final Item.Properties properties)
    {
        super("clipboard", properties.stacksTo(1));
    }

    @Override
    @NotNull
    public InteractionResult useOn(final UseOnContext ctx)
    {
        final ItemStack clipboard = ctx.getPlayer().getItemInHand(ctx.getHand());

        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof TileEntityColonyBuilding buildingEntity)
        {
            buildingEntity.writeColonyToItemStack(clipboard);

            if (!ctx.getLevel().isClientSide())
            {
                MessageUtils.format(COM_MINECOLONIES_CLIPBOARD_COLONY_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
            }
        }
        else if (ctx.getLevel().isClientSide())
        {
            openWindow(clipboard, ctx.getLevel(), ctx.getPlayer());
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
    public InteractionResult use(
            final Level worldIn,
            final Player playerIn,
            final InteractionHand hand)
    {
        final ItemStack clipboard = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide()) {
            return InteractionResult.SUCCESS.heldItemTransformedTo(clipboard);
        }

        openWindow(clipboard, worldIn, playerIn);

        return InteractionResult.SUCCESS.heldItemTransformedTo(clipboard);
    }

    /**
     * Opens the clipboard window if there is a valid colony linked
     * @param stack the item
     * @param player the player entity opening the window
     */
    private static void openWindow(ItemStack stack, Level world, Player player)
    {        
        final IColonyView colonyView = ColonyId.readColonyViewFromItemStack(stack);
        if (colonyView != null)
        {
            boolean hide = false;

            final CustomData current = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            final CompoundTag compound = current.copyTag();

            if (compound.contains(TAG_HIDEUNIMPORTANT))
            {
                hide = compound.getBooleanOr(TAG_HIDEUNIMPORTANT, false);
            }

            new WindowClipBoard(colonyView, hide).open();
        }
        else
        {
            player.sendOverlayMessage(Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_CLIPBOARD_NEED_COLONY));
        }
    }
}
