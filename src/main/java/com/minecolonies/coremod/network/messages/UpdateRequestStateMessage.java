package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.coremod.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Updates the request state of a request..
 */
public class UpdateRequestStateMessage extends AbstractMessage<UpdateRequestStateMessage, IMessage>
{
    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The requestId
     */
    private String requestId;

    /**
     * The request state to set.
     */
    private RequestState state;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateRequestStateMessage()
    {
        super();
    }

    /**
     * Create an update request state message.
     * @param colonyId the colony id.
     * @param requestId the request id.
     * @param state the state to set.
     */
    public UpdateRequestStateMessage(final int colonyId, final String requestId, final RequestState state)
    {
        super();
        this.colonyId = colonyId;
        this.requestId = requestId;
        this.state = state;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        requestId = ByteBufUtils.readUTF8String(buf);
        state = RequestState.values()[buf.readInt()];

    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, requestId);
        buf.writeInt(state.ordinal());
    }

    @Override
    public void messageOnServerThread(final UpdateRequestStateMessage message, final EntityPlayerMP player)
    {
        //todo orion I think you'll have to do this here.
        final IColony colony = ColonyManager.getColony(message.colonyId);
        //colony.getRequestManager().updateRequestState(message.requestId, message.state);
    }
}
