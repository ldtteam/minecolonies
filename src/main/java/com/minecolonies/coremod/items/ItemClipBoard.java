package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the clipboard item.
 */
public class ItemClipBoard extends AbstractItemMinecolonies
{
    /**
     * Tag of the colony.
     */
    private static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     * @param properties the properties.
     */
    public ItemClipBoard(final Item.Properties properties)
    {
        super("clipboard", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    @NotNull
    public ActionResultType onItemUse(final ItemUseContext ctx)
    {
        final ItemStack clipboard = ctx.getPlayer().getHeldItem(ctx.getHand());

        final CompoundNBT compound = checkForCompound(clipboard);
        final TileEntity entity = ctx.getWorld().getTileEntity(ctx.getPos());

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            if (!ctx.getWorld().isRemote)
            {
                LanguageHandler.sendPlayerMessage(ctx.getPlayer(),
                  TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET,
                  ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.keySet().contains(TAG_COLONY))
        {
            if (ctx.getWorld().isRemote)
            {
                final IColonyView colonyView = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY), ctx.getWorld().dimension.getType().getId());
                MineColonies.proxy.openClipBoardWindow(colonyView);
            }
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
    public ActionResult<ItemStack> onItemRightClick(
      final World worldIn,
      final PlayerEntity playerIn,
      final Hand hand)
    {
        final ItemStack cllipboard = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            return new ActionResult<>(ActionResultType.SUCCESS, cllipboard);
        }

        final CompoundNBT compound = checkForCompound(cllipboard);

        if (compound.keySet().contains(TAG_COLONY))
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY), worldIn.dimension.getType().getId());
            MineColonies.proxy.openClipBoardWindow(colonyView);
        }
        else
        {
            LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_NEED_COLONY);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, cllipboard);
    }

    /**
     * Check for the compound and return it.
     * If not available create and return it.
     *
     * @param scepter the scepter to check in for.
     * @return the compound of the scepter.
     */
    private static CompoundNBT checkForCompound(final ItemStack scepter)
    {
        if (!scepter.hasTag())
        {
            scepter.setTag(new CompoundNBT());
        }
        return scepter.getTag();
    }
}
