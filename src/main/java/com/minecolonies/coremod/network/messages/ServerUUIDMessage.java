package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.network.PacketUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
        serverUUID = PacketUtils.readUUID(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, ColonyManager.getServerUUID());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the server UUID.
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
