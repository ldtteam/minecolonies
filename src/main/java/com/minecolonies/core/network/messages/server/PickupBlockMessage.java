package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.Colony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_BUILDING_PICKUP_PLAYER_INVENTORY_FULL;

/**
 * Pickup the town hall block.
 */
public class PickupBlockMessage implements IMessage
{
    /**
     * Position the player wants to found the colony at.
     */
    BlockPos pos;

    public PickupBlockMessage()
    {
        super();
    }

    public PickupBlockMessage(final BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer sender = ctxIn.getSender();
        final Level world = ctxIn.getSender().level;

        if (sender == null)
        {
            return;
        }

        if (IColonyManager.getInstance().getColonyByPosFromWorld(world, pos) instanceof Colony)
        {
            return;
        }

        final ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock(), 1);
        final CompoundTag compoundNBT = new CompoundTag();
        stack.setTag(compoundNBT);
        if (InventoryUtils.addItemStackToProvider(sender, stack))
        {
            world.destroyBlock(pos, false);
        }
        else
        {
            MessageUtils.format(WARNING_BUILDING_PICKUP_PLAYER_INVENTORY_FULL).sendTo(sender);
        }

    }
}
