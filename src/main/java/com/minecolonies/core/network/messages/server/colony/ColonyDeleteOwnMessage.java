package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.event.ColonyDeletedEvent;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message for deleting an owned colony
 */
public class ColonyDeleteOwnMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "colony_delete_own", ColonyDeleteOwnMessage::new);

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {        
        // noop
    }

    protected ColonyDeleteOwnMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    public ColonyDeleteOwnMessage()
    {
        super(TYPE);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player)
    {
        if (player == null)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(player.level(), player);
        if (colony != null)
        {
            IColonyManager.getInstance().deleteColonyByDimension(colony.getID(), false, colony.getDimension());
            MessageUtils.format(MESSAGE_INFO_COLONY_DESTROY_SUCCESS).sendTo(player);

            NeoForge.EVENT_BUS.post(new ColonyDeletedEvent(colony));
        }
        else
        {
            MessageUtils.format(MESSAGE_INFO_COLONY_NOT_FOUND).sendTo(player);
        }
    }
}
