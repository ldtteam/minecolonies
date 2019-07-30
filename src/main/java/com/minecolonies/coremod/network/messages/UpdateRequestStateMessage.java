package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Updates the request state of a request.
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
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack = ItemStackUtils.EMPTY;

    /**
     * The request state to set.
     */
    private RequestState state;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateRequestStateMessage()
    {
        super();
    }

    /**
     * Create an update request state message.
     *
     * @param colonyId  the colony id.
     * @param requestId the request id.
     * @param state     the state to set.
     * @param itemStack the involved itemStack.
     */
    public UpdateRequestStateMessage(final int colonyId, final IToken requestId, final RequestState state, final ItemStack itemStack)
    {
        super();
        this.colonyId = colonyId;
        this.token = requestId;
        this.state = state;
        this.itemStack = itemStack;

    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        token = StandardFactoryController.getInstance().deserialize(ByteBufUtils.readTag(buf));
        state = RequestState.values()[buf.readInt()];
        if (state == RequestState.OVERRULED)
        {
            itemStack = ByteBufUtils.readItemStack(buf);
        }
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeTag(buf, StandardFactoryController.getInstance().serialize(token));
        buf.writeInt(state.ordinal());
        if (state == RequestState.OVERRULED)
        {
            ByteBufUtils.writeItemStack(buf, itemStack);
        }
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final UpdateRequestStateMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony instanceof Colony)
        {
            if (message.state == RequestState.OVERRULED)
            {
                colony.getRequestManager().overruleRequest(message.token, message.itemStack);
                return;
            }
            colony.getRequestManager().updateRequestState(message.token, message.state);
        }
    }
}
