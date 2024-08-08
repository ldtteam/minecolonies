package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Updates the request state of a request.
 */
public class UpdateRequestStateMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "update_request_state", UpdateRequestStateMessage::new);

    /**
     * The requestId
     */
    private final IToken<?> token;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final ItemStack itemStack;

    /**
     * The request state to set.
     */
    private final RequestState state;

    /**
     * Create an update request state
     *
     * @param requestId the request id.
     * @param state     the state to set.
     * @param itemStack the involved itemStack.
     * @param colony    the colony of the network message
     */
    public UpdateRequestStateMessage(final IColony colony, final IToken<?> requestId, final RequestState state, final ItemStack itemStack)
    {
        super(TYPE, colony);
        this.token = requestId;
        this.state = state;
        this.itemStack = itemStack;
    }

    protected UpdateRequestStateMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        token = StandardFactoryController.getInstance().deserialize(buf);
        state = RequestState.values()[buf.readInt()];
        itemStack = state == RequestState.OVERRULED ? Utils.deserializeCodecMess(buf) : ItemStack.EMPTY;
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        StandardFactoryController.getInstance().serialize(buf, token);
        buf.writeInt(state.ordinal());
        if (state == RequestState.OVERRULED)
        {
            Utils.serializeCodecMess(buf, itemStack);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        if (state == RequestState.OVERRULED)
        {
            colony.getRequestManager().overruleRequest(token, itemStack);
            return;
        }
        colony.getRequestManager().updateRequestState(token, state);
    }
}
