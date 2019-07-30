package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.network.PacketUtils;
import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class handling the Server UUID Message.
 */
public class ServerUUIDMessage implements IMessage
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        serverUUID = PacketUtils.readUUID(buf);
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        PacketUtils.writeUUID(buf, IColonyManager.getInstance().getServerUUID());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the server UUID.
     *
     * @param message Message
     * @param ctx     Context
     */
    @Override
    protected void messageOnClientThread(final ServerUUIDMessage message, final MessageContext ctx)
    {
        IColonyManager.getInstance().setServerUUID(message.serverUUID);
    }
}
