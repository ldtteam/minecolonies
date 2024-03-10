package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message for deleting an owned colony
 */
public class ColonyAbandonOwnMessage implements IMessage
{
    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {

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
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.level, player);
        if (colony != null)
        {
            colony.getPermissions().setOwnerAbandoned();
            colony.getPermissions().addPlayer(player.getGameProfile(), colony.getPermissions().getRankOfficer());
            MessageUtils.format(MESSAGE_INFO_COLONY_ABANDON_SUCCESS).sendTo(player);
        }
        else
        {
            MessageUtils.format(MESSAGE_INFO_COLONY_NOT_FOUND).sendTo(player);
        }
    }
}
