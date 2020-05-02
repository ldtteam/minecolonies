package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Updates the request state of a request.
 */
public class UpdateRequestStateMessage extends AbstractColonyServerMessage
{
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
     * Empty constructor used when registering the 
     */
    public UpdateRequestStateMessage()
    {
        super();
    }

    /**
     * Create an update request state 
     *
     * @param requestId the request id.
     * @param state     the state to set.
     * @param itemStack the involved itemStack.
     * @param colony the colony of the network message
     */
    public UpdateRequestStateMessage(final IColony colony, final IToken requestId, final RequestState state, final ItemStack itemStack)
    {
        super(colony);
        this.token = requestId;
        this.state = state;
        this.itemStack = itemStack;

    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        token = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
        state = RequestState.values()[buf.readInt()];
        if (state == RequestState.OVERRULED)
        {
            itemStack = buf.readItemStack();
        }
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(token));
        buf.writeInt(state.ordinal());
        if (state == RequestState.OVERRULED)
        {
            buf.writeItemStack(itemStack);
        }
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (state == RequestState.OVERRULED)
        {
            colony.getRequestManager().overruleRequest(token, itemStack);
            return;
        }
        colony.getRequestManager().updateRequestState(token, state);
    }
}
