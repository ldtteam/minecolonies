package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
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
    private IToken token;

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
    public UpdateRequestStateMessage(final int colonyId, final IToken requestId, final RequestState state)
    {
        super();
        this.colonyId = colonyId;
        this.token = requestId;
        this.state = state;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        token = StandardFactoryController.getInstance().deserialize(ByteBufUtils.readTag(buf));
        state = RequestState.values()[buf.readInt()];

    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeTag(buf, StandardFactoryController.getInstance().serialize(token));
        buf.writeInt(state.ordinal());
    }

    @Override
    public void messageOnServerThread(final UpdateRequestStateMessage message, final EntityPlayerMP player)
    {
        final IColony colony = ColonyManager.getColony(message.colonyId);
        if(colony != null)
        {
            colony.getRequestManager().updateRequestState(message.token, RequestState.OVERRULED);
        }
    }
}
