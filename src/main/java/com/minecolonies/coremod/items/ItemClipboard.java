package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET;

/**
 * Class describing the clipboard item.
 */
public class ItemClipboard extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    private static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the Clipboard item.
     *
     * @param properties the properties.
     */
    public ItemClipboard(final Item.Properties properties)
    {
        super("clipboard", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    @NotNull
    public ActionResultType useOn(final ItemUseContext ctx)
    {
        final ItemStack clipboard = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundNBT compound = checkForCompound(clipboard);
        final TileEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            if (!ctx.getLevel().isClientSide)
            {
                MessageUtils.format(COM_MINECOLONIES_CLIPBOARD_COLONY_SET, ((TileEntityColonyBuilding) entity).getColony().getName()).sendTo(ctx.getPlayer());
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(compound, ctx.getLevel(), ctx.getPlayer());
        }

        return ActionResultType.SUCCESS;
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
    public ActionResult<ItemStack> use(
            final World worldIn,
            final PlayerEntity playerIn,
            final Hand hand)
    {
        final ItemStack clipboard = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide) {
            return new ActionResult<>(ActionResultType.SUCCESS, clipboard);
        }

        openWindow(checkForCompound(clipboard), worldIn, playerIn);

        return new ActionResult<>(ActionResultType.SUCCESS, clipboard);
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param clipboard the clipboard to check for.
     * @return the compound of the clipboard.
     */
    private static CompoundNBT checkForCompound(final ItemStack clipboard)
    {
        if (!clipboard.hasTag()) clipboard.setTag(new CompoundNBT());
        return clipboard.getTag();
    }

    /**
     * Opens the clipboard window if there is a valid colony linked
     * @param compound the item compound
     * @param player the player entity opening the window
     */
    private static void openWindow(CompoundNBT compound, World world, PlayerEntity player)
    {
        if (compound.getAllKeys().contains(TAG_COLONY))
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY), world.dimension());
            if (colonyView != null) MineColonies.proxy.openClipboardWindow(colonyView);
        }
        else
        {
            player.displayClientMessage(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_CLIPBOARD_NEED_COLONY), true);
        }
    }
}
