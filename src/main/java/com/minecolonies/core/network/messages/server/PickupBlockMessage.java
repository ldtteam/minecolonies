package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_BUILDING_PICKUP_PLAYER_INVENTORY_FULL;

/**
 * Pickup the town hall block.
 */
public class PickupBlockMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "pickup_block", PickupBlockMessage::new);

    /**
     * Position the player wants to found the colony at.
     */
    BlockPos pos;

    public PickupBlockMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        pos = buf.readBlockPos();
    }

    public PickupBlockMessage(final BlockPos pos)
    {
        super(TYPE);
        this.pos = pos;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer sender)
    {
        if (sender == null)
        {
            return;
        }
        final Level world = sender.level();

        if (IColonyManager.getInstance().getColonyByPosFromWorld(world, pos) instanceof Colony)
        {
            return;
        }

        final ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock(), 1);
        final CompoundTag compoundNBT = new CompoundTag();
        stack.setTag(compoundNBT);
        if (InventoryUtils.addItemStackToItemHandler(new InvWrapper(sender.getInventory()), stack))
        {
            world.destroyBlock(pos, false);
        }
        else
        {
            MessageUtils.format(WARNING_BUILDING_PICKUP_PLAYER_INVENTORY_FULL).sendTo(sender);
        }

    }
}
