package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.UseAnim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

/**
 * Scroll items base class, does colony registering/checks.
 */
public abstract class AbstractItemScroll extends AbstractItemMinecolonies
{
    public static final String TAG_COLONY_DIM       = "colony_dim";
    public static final String TAG_BUILDING_POS     = "building_pos";
    public static final int    FAIL_RESPONSES_TOTAL = 10;

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name       The name of this item
     * @param properties the properties.
     */
    public AbstractItemScroll(final String name, final Properties properties)
    {
        super(name, properties);
    }

    @Override
    public int getUseDuration(ItemStack itemStack)
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
            player.displayClientMessage(Component.translatable(MESSAGE_SCROLL_NEED_COLONY), true);
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
        final CompoundTag compound = checkForCompound(scroll);
        if (te instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY_ID, ((AbstractTileEntityColonyBuilding) te).getColonyId());
            compound.putString(TAG_COLONY_DIM, ((AbstractTileEntityColonyBuilding) te).getColony().getWorld().dimension().location().toString());
            BlockPosUtil.write(compound, TAG_BUILDING_POS, ctx.getClickedPos());
            MessageUtils.format(MESSAGE_SCROLL_REGISTERED, ((AbstractTileEntityColonyBuilding) te).getColony().getName()).sendTo(ctx.getPlayer());
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Whether this items need to register to a colony
     *
     * @return true if so
     */
    protected abstract boolean needsColony();

    private static CompoundTag checkForCompound(final ItemStack item)
    {
        if (!item.hasTag())
        {
            item.setTag(new CompoundTag());
        }
        return item.getTag();
    }

    /**
     * Get the colony from the stack
     *
     * @param stack to use
     * @return colony
     */
    protected IColony getColony(final ItemStack stack)
    {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_COLONY_ID) || !stack.getTag().contains(TAG_COLONY_DIM))
        {
            return null;
        }

        return IColonyManager.getInstance().getColonyByDimension(stack.getTag().getInt(TAG_COLONY_ID), ResourceKey.create(Registries.DIMENSION, new ResourceLocation(stack.getTag().getString(TAG_COLONY_DIM))));
    }

    /**
     * Get the colony view from the stack
     *
     * @param stack to use
     * @return colony
     */
    protected IColony getColonyView(final ItemStack stack)
    {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_COLONY_ID) || !stack.getTag().contains(TAG_COLONY_DIM))
        {
            return null;
        }

        return IColonyManager.getInstance().getColonyView(stack.getTag().getInt(TAG_COLONY_ID), ResourceKey.create(Registries.DIMENSION, new ResourceLocation(stack.getTag().getString(TAG_COLONY_DIM))));
    }
}
