package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.configuration.Configurations;
import com.ldtteam.structurize.management.Structures;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Class handling the colony styles messages.
 */
public class ColonyStylesMessage implements IMessage
{
    private boolean             allowPlayerSchematics;
    private Map<String, String> md5Map;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyStylesMessage()
    {
        super();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        allowPlayerSchematics = buf.readBoolean();
        md5Map = readMD5MapFromByteBuf(buf);
    }

    @NotNull
    private static Map<String, String> readMD5MapFromByteBuf(@NotNull final ByteBuf buf)
    {
        @NotNull final Map<String, String> map = new HashMap<>();

        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            final String filename = buf.readString();
            final String md5 = buf.readString();
            map.put(filename, md5);
        }
        return map;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(Configurations.gameplay.allowPlayerSchematics);
        writeMD5MapToByteBuf(buf);
    }

    private static void writeMD5MapToByteBuf(@NotNull final ByteBuf buf)
    {
        final Map<String, String> md5s = Structures.getMD5s();
        buf.writeInt(md5s.size());
        for (final Map.Entry<String, String> entry : md5s.entrySet())
        {
            buf.writeString(entry.getKey());
            buf.writeString(entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the styles of the huts to the given value in the message
     *
     * @param message Message
     * @param ctx     Context
     */
    @Override
    protected void messageOnClientThread(final ColonyStylesMessage message, final MessageContext ctx)
    {
        Structures.setAllowPlayerSchematics(message.allowPlayerSchematics);
        Structures.setMD5s(message.md5Map);
    }
}
