package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Updates the request state of a request.
 */
public class UpdateRequestStateMessage implements IMessage
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
     * The dimension of the 
     */
    private int dimension;

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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        token = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
        state = RequestState.values()[buf.readInt()];
        if (state == RequestState.OVERRULED)
        {
            itemStack = buf.readItemStack();
        }
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(token));
        buf.writeInt(state.ordinal());
        if (state == RequestState.OVERRULED)
        {
            buf.writeItemStack(itemStack);
        }
        buf.writeInt(dimension);
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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony instanceof Colony)
        {
            if (state == RequestState.OVERRULED)
            {
                colony.getRequestManager().overruleRequest(token, itemStack);
                return;
            }
            colony.getRequestManager().updateRequestState(token, state);
        }
    }
}
