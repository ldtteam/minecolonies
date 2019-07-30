package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * If this is a new subscription.
     */
    private boolean isNewSubscription;

    /**
     * The buffer with the data.
     */
    private ByteBuf colonyBuffer;

    /**
     * The dimension of the colony.
     */
    private int dim;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewMessage()
    {
        super();
    }

    /**
     * Add or Update a ColonyView on the client.
     *
     * @param colony            Colony of the view to update.
     * @param buf               the bytebuffer.
     * @param isNewSubscription Boolean whether or not this is a new subscription.
     */
    public ColonyViewMessage(@NotNull final Colony colony, final ByteBuf buf, final boolean isNewSubscription)
    {
        this.colonyId = colony.getID();
        this.isNewSubscription = isNewSubscription;
        this.dim = colony.getDimension();
        this.colonyBuffer = buf.copy();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        final ByteBuf newBuf = buf.retain();
        colonyId = newBuf.readInt();
        isNewSubscription = newBuf.readBoolean();
        dim = newBuf.readInt();
        colonyBuffer = newBuf;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeInt(dim);
        buf.writeBytes(colonyBuffer);
    }

    @Override
    protected void messageOnClientThread(final ColonyViewMessage message, final MessageContext ctx)
    {
        if (MineColonies.proxy.getWorldFromMessage(ctx) != null)
        {
            IColonyManager.getInstance().handleColonyViewMessage(message.colonyId, message.colonyBuffer, MineColonies.proxy.getWorldFromMessage(ctx), message.isNewSubscription, message.dim);
        }
    }
}
