package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Message class which manages the message to toggle automatic or manual job allocation.
 */
public class ToggleJobMessage implements IMessage, IMessageHandler<ToggleJobMessage, IMessage>
{
    /**
     * The Colony ID;
     */
    private int     colonyId;
    /**
     * Toggle the job allocation to true or false.
     */
    private boolean toggle;

    /**
     * Empty public constructor.
     */
    public ToggleJobMessage()
    {
        // Required for netty.
    }

    /**
     * Creates object for the player to turn manual allocation or or off.
     *
     * @param colony view of the colony to read data from
     * @param toggle toggle the job to manually or automatically
     */
    public ToggleJobMessage(@Nonnull ColonyView colony, boolean toggle)
    {
        this.colonyId = colony.getID();
        this.toggle = toggle;
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@Nonnull ByteBuf buf)
    {
        colonyId = buf.readInt();
        toggle = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@Nonnull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(toggle);
    }

    /**
     * Called when a message has been received.
     *
     * @param message the message.
     * @param ctx     the context.
     * @return possible response, in this case -&gt; null.
     */
    @Nullable
    @Override
    public IMessage onMessage(@Nonnull ToggleJobMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            colony.setManualHiring(message.toggle);
        }
        return null;
    }
}
