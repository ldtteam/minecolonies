package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.ChangeFreeToInteractBlockMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Permission scepter. used to add free to interact blocks or positions to the colonies permission list
 */
public class ItemScepterPermission extends AbstractItemMinecolonies
{
    /**
     * The NBT tag of the mode
     */
    private static final String TAG_ITEM_MODE = "scepterMode";

    /**
     * the scepters block mode tag value
     */
    private static final String TAG_VALUE_MODE_BLOCK = "modeBlock";

    /**
     * the scepters location mode tag value
     */
    private static final String TAG_VALUE_MODE_LOCATION = "modeLocation";

    /**
     * constructor.
     * <p>
     * - set the name - set max damage value - set creative tab - set max stack size
     *
     * @param properties the properties.
     */
    public ItemScepterPermission(final Item.Properties properties)
    {
        super("scepterpermission", properties.stacksTo(1).durability(2).tab(ModCreativeTabs.MINECOLONIES));
    }

    @NotNull
    private static ActionResultType handleAddBlockType(
      final PlayerEntity playerIn,
      final World worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final BlockState blockState = iColonyView.getWorld().getBlockState(pos);
        final Block block = blockState.getBlock();

        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(
          iColonyView,
          block,
          ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        Network.getNetwork().sendToServer(message);

        return ActionResultType.SUCCESS;
    }

    @NotNull
    private static ActionResultType handleAddLocation(
      final PlayerEntity playerIn,
      final World worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(iColonyView, pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        Network.getNetwork().sendToServer(message);

        return ActionResultType.SUCCESS;
    }

    /**
     * Used when clicking on block in world.
     *
     * @return the result
     */
    @Override
    @NotNull
    public ActionResultType useOn(final ItemUseContext ctx)
    {
        if (!ctx.getLevel().isClientSide)
        {
            return ActionResultType.SUCCESS;
        }
        final ItemStack scepter = ctx.getPlayer().getItemInHand(ctx.getHand());
        if (!scepter.hasTag())
        {
            scepter.setTag(new CompoundNBT());
        }

        final IColonyView iColonyView = IColonyManager.getInstance().getClosestColonyView(ctx.getLevel(), ctx.getClickedPos());
        if (iColonyView == null)
        {
            return ActionResultType.FAIL;
        }
        final CompoundNBT compound = scepter.getTag();
        return handleItemAction(compound, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), iColonyView);
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
        final ItemStack scepter = playerIn.getItemInHand(hand);
        if (worldIn.isClientSide)
        {
            return new ActionResult<>(ActionResultType.SUCCESS, scepter);
        }
        if (!scepter.hasTag())
        {
            scepter.setTag(new CompoundNBT());
        }
        final CompoundNBT compound = scepter.getTag();

        toggleItemMode(playerIn, compound);

        return new ActionResult<>(ActionResultType.SUCCESS, scepter);
    }

    private static void toggleItemMode(final PlayerEntity playerIn, final CompoundNBT compound)
    {
        final String itemMode = compound.getString(TAG_ITEM_MODE);

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                compound.putString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_SET_MODE, MessageUtils.format(TOOL_PERMISSION_SCEPTER_MODE_LOCATION).create()).sendTo(playerIn);
                break;
            case TAG_VALUE_MODE_LOCATION:
            default:
                compound.putString(TAG_ITEM_MODE, TAG_VALUE_MODE_BLOCK);
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_SET_MODE, MessageUtils.format(TOOL_PERMISSION_SCEPTER_MODE_BLOCK).create()).sendTo(playerIn);
                break;
        }
    }

    @NotNull
    private static ActionResultType handleItemAction(
      final CompoundNBT compound,
      final PlayerEntity playerIn,
      final World worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final String tagItemMode = compound.getString(TAG_ITEM_MODE);

        switch (tagItemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                return handleAddBlockType(playerIn, worldIn, pos, iColonyView);
            case TAG_VALUE_MODE_LOCATION:
                return handleAddLocation(playerIn, worldIn, pos, iColonyView);
            default:
                toggleItemMode(playerIn, compound);
                return handleItemAction(compound, playerIn, worldIn, pos, iColonyView);
        }
    }
}
