package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage implements IMessage, IMessageHandler<ServerUUIDMessage, IMessage>
{
    private UUID serverUUID;

    /**
     * Empty constructor used when registering the message.
     */
    public ServerUUIDMessage()
    {
        super();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        serverUUID = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeLong(ColonyManager.getServerUUID().getMostSignificantBits());
        buf.writeLong(ColonyManager.getServerUUID().getLeastSignificantBits());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the styles of the huts to the given value in the message
     *
     * @param message Message
     * @param ctx     Context
     * @return Null
     */
    @Nullable
    @Override
    public IMessage onMessage(@NotNull final ServerUUIDMessage message, final MessageContext ctx)
    {
        ColonyManager.setServerUUID(message.serverUUID);
        return null;
    }
}
