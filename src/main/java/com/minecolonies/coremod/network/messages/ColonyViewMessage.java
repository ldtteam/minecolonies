package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewMessage implements IMessage, IMessageHandler<ColonyViewMessage, IMessage>
{
    private int     colonyId;
    private boolean isNewSubscription;
    private ByteBuf colonyBuffer;

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
     * @param isNewSubscription Boolean whether or not this is a new subscription.
     */
    public ColonyViewMessage(@NotNull final Colony colony, final boolean isNewSubscription)
    {
        this.colonyId = colony.getID();
        this.isNewSubscription = isNewSubscription;
        this.colonyBuffer = Unpooled.buffer();
        ColonyView.serializeNetworkData(colony, colonyBuffer, isNewSubscription);
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final ByteBuf newBuf = buf.retain();
        colonyId = newBuf.readInt();
        isNewSubscription = newBuf.readBoolean();
        colonyBuffer = newBuf;
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeBytes(colonyBuffer);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final ColonyViewMessage message, final MessageContext ctx)
    {
        if(MineColonies.proxy.getWorldFromMessage(ctx) != null)
        {
            return ColonyManager.handleColonyViewMessage(message.colonyId, message.colonyBuffer, MineColonies.proxy.getWorldFromMessage(ctx), message.isNewSubscription);
        }
        return null;
    }
}
