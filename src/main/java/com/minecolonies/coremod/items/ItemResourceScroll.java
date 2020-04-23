package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;

/**
 * Class describing the resource scroll item.
 */
public class ItemResourceScroll extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the resource scroll item.
     * @param properties the properties.
     */
    public ItemResourceScroll(final Item.Properties properties)
    {
        super("resourcescroll", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES));
    }

    /**
     * Used when clicking on block in world.
     *
     * @param ctx the context of use.
     * @return the result
     */
    @Override
    @NotNull
    public ActionResultType onItemUse(ItemUseContext ctx)
    {
        final ItemStack scroll = ctx.getPlayer().getHeldItem(ctx.getHand());

        final CompoundNBT compound = checkForCompound(scroll);
        final TileEntity entity = ctx.getWorld().getTileEntity(ctx.getPos());

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY_ID, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            BlockPosUtil.write(compound, TAG_BUILDER, ((AbstractTileEntityColonyBuilding) entity).getPosition());

            if (!ctx.getWorld().isRemote)
            {
                LanguageHandler.sendPlayerMessage(ctx.getPlayer(),
                  TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET,
                  ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.keySet().contains(TAG_COLONY_ID) && compound.keySet().contains(TAG_BUILDER) && ctx.getWorld().isRemote)
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos);
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

        if (compound.keySet().contains(TAG_COLONY_ID) && compound.keySet().contains(TAG_BUILDER))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos);
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
     * @param item the item to check in for.
     */
    private static CompoundNBT checkForCompound(final ItemStack item)
    {
        if (!item.hasTag())
        {
            item.setTag(new CompoundNBT());
        }
        return item.getTag();
    }
}
