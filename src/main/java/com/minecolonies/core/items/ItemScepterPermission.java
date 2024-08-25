package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.items.component.Desc;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.network.messages.server.colony.ChangeFreeToInteractBlockMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Permission scepter. used to add free to interact blocks or positions to the colonies permission list
 */
public class ItemScepterPermission extends AbstractItemMinecolonies implements IBlockOverlayItem
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

    private static final int GREEN_OVERLAY = 0xFF00FF00;
    private static final int BLOCK_OVERLAY_RANGE_XZ = 32;
    private static final int BLOCK_OVERLAY_RANGE_Y = 6;

    /**
     * constructor.
     * <p>
     * - set the name - set max damage value - set creative tab - set max stack size
     *
     * @param properties the properties.
     */
    public ItemScepterPermission(final Item.Properties properties)
    {
        super("scepterpermission", properties.stacksTo(1).durability(2));
    }

    @NotNull
    private static InteractionResult handleAddBlockType(
      final Player playerIn,
      final Level worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final BlockState blockState = iColonyView.getWorld().getBlockState(pos);
        final Block block = blockState.getBlock();

        final ChangeFreeToInteractBlockMessage.MessageType type = Screen.hasControlDown()
                ? ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK
                : ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK;
        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(
          iColonyView,
          block,
          type);
        message.sendToServer();

        return InteractionResult.SUCCESS;
    }

    @NotNull
    private static InteractionResult handleAddLocation(
      final Player playerIn,
      final Level worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final ChangeFreeToInteractBlockMessage.MessageType type = Screen.hasControlDown()
                ? ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK
                : ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK;
        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(iColonyView, pos, type);
        message.sendToServer();

        return InteractionResult.SUCCESS;
    }

    /**
     * Used when clicking on block in world.
     *
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResult useOn(final UseOnContext ctx)
    {
        if (!ctx.getLevel().isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        final ItemStack scepter = ctx.getPlayer().getItemInHand(ctx.getHand());
        final IColonyView iColonyView = IColonyManager.getInstance().getClosestColonyView(ctx.getLevel(), ctx.getClickedPos());
        if (iColonyView == null)
        {
            return InteractionResult.FAIL;
        }
        return handleItemAction(scepter, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), iColonyView);
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
        final ItemStack scepter = playerIn.getItemInHand(hand);
        if (worldIn.isClientSide)
        {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, scepter);
        }

        toggleItemMode(playerIn, scepter);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, scepter);
    }

    private static void toggleItemMode(final Player playerIn, final ItemStack stack)
    {
        final String itemMode = Desc.readFromItemStack(stack).desc();

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                new Desc(TAG_VALUE_MODE_LOCATION).writeToItemStack(stack);
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_SET_MODE, MessageUtils.format(TOOL_PERMISSION_SCEPTER_MODE_LOCATION).create()).sendTo(playerIn);
                break;
            case TAG_VALUE_MODE_LOCATION:
            default:
                new Desc(TAG_VALUE_MODE_BLOCK).writeToItemStack(stack);
                MessageUtils.format(TOOL_PERMISSION_SCEPTER_SET_MODE, MessageUtils.format(TOOL_PERMISSION_SCEPTER_MODE_BLOCK).create()).sendTo(playerIn);
                break;
        }
    }

    @NotNull
    @Override
    public List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull final ItemStack stack)
    {
        final List<OverlayBox> boxes = new ArrayList<>();
        final IColonyView colony = IColonyManager.getInstance().getClosestColonyView(world, player.blockPosition());
        if (colony == null || !colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
        {
            return boxes;
        }

        final String itemMode = Desc.readFromItemStack(stack).desc();
        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                final Set<Block> freeBlocks = new HashSet<>(colony.getFreeBlocks());
                for (final BlockPos pos : BlockPos.withinManhattan(player.blockPosition(), BLOCK_OVERLAY_RANGE_XZ, BLOCK_OVERLAY_RANGE_Y, BLOCK_OVERLAY_RANGE_XZ))
                {
                    if (world.isLoaded(pos) && freeBlocks.contains(world.getBlockState(pos).getBlock()))
                    {
                        boxes.add(new OverlayBox(pos, GREEN_OVERLAY, 0.02f, true));
                    }
                }
                break;
            case TAG_VALUE_MODE_LOCATION:
            default:
                for (final BlockPos pos : colony.getFreePositions())
                {
                    boxes.add(new OverlayBox(pos, GREEN_OVERLAY, 0.02f, true));
                }
                break;
        }

        return boxes;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flags)
    {
        final String itemMode = Desc.readFromItemStack(stack).desc();
        final MutableComponent mode;
        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                mode = Component.translatable(TOOL_PERMISSION_SCEPTER_MODE_BLOCK);
                break;
            case TAG_VALUE_MODE_LOCATION:
            default:
                mode = Component.translatable(TOOL_PERMISSION_SCEPTER_MODE_LOCATION);
                break;
        }
        tooltip.add(Component.translatable(TOOL_PERMISSION_SCEPTER_MODE, mode.withStyle(ChatFormatting.YELLOW)));

        super.appendHoverText(stack, ctx, tooltip, flags);
    }

    @NotNull
    private static InteractionResult handleItemAction(
      final ItemStack stack,
      final Player playerIn,
      final Level worldIn,
      final BlockPos pos,
      final IColonyView iColonyView)
    {
        final String tagItemMode = Desc.readFromItemStack(stack).desc();

        switch (tagItemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                return handleAddBlockType(playerIn, worldIn, pos, iColonyView);
            case TAG_VALUE_MODE_LOCATION:
                return handleAddLocation(playerIn, worldIn, pos, iColonyView);
            default:
                toggleItemMode(playerIn, stack);
                return handleItemAction(stack, playerIn, worldIn, pos, iColonyView);
        }
    }
}
