package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

/**
 * Scroll items base class, does colony registering/checks.
 */
public abstract class AbstractItemScroll extends Item
{
    public static final int    FAIL_RESPONSES_TOTAL = 10;

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name       The name of this item
     * @param properties the properties.
     */
    public AbstractItemScroll(final Properties properties)
    {
        super(properties);
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity livingEntity)
    {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack)
    {
        return UseAnim.BOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity entityLiving)
    {
        if (!(entityLiving instanceof ServerPlayer) || world.isClientSide)
        {
            return itemStack;
        }

        final ServerPlayer player = (ServerPlayer) entityLiving;

        if (!needsColony())
        {
            return onItemUseSuccess(itemStack, world, player);
        }

        final IColony colony = getColony(itemStack);
        if (colony == null)
        {
            player.displayClientMessage(Component.translatableEscape(MESSAGE_SCROLL_NEED_COLONY), true);
            return itemStack;
        }

        if (!colony.getPermissions().hasPermission(player, Action.RIGHTCLICK_BLOCK))
        {
            MessageUtils.format(MESSAGE_SCROLL_NO_PERMISSION).sendTo(player);
            return itemStack;
        }

        return onItemUseSuccess(itemStack, world, player);
    }

    /**
     * Called when the item gets used
     *
     * @param itemStack stack thats used
     * @param world     world its used in
     * @param player    player its used by
     * @return stack
     */
    protected abstract ItemStack onItemUseSuccess(final ItemStack itemStack, final Level world, final ServerPlayer player);

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        // Sneak rightclick
        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext ctx)
    {
        // Right click on block
        if (ctx.getLevel().isClientSide || !ctx.getPlayer().isShiftKeyDown() || !needsColony())
        {
            return InteractionResult.PASS;
        }

        final BlockEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        final ItemStack scroll = ctx.getPlayer().getItemInHand(ctx.getHand());


        if (te instanceof TileEntityColonyBuilding colonyBuilding)
        {
            colonyBuilding.getBuilding().writeToItemStack(scroll);
            MessageUtils.format(MESSAGE_SCROLL_REGISTERED, colonyBuilding.getColony().getName()).sendTo(ctx.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Whether this items need to register to a colony
     *
     * @return true if so
     */
    protected abstract boolean needsColony();

    /**
     * Get the colony from the stack
     *
     * @param stack to use
     * @return colony
     * @deprecated use inline
     */
    @Deprecated(forRemoval = true, since = "1.21")
    protected IColony getColony(final ItemStack stack)
    {
        return ColonyId.readColonyFromItemStack(stack);
    }

    /**
     * Get the colony view from the stack
     *
     * @param stack to use
     * @return colony
     * @deprecated use inline
     */
    @Deprecated(forRemoval = true, since = "1.21")
    protected IColony getColonyView(final ItemStack stack)
    {
        return ColonyId.readColonyViewFromItemStack(stack);
    }
}
