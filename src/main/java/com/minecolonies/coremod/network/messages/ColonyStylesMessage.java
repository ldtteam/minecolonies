package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.configuration.Configurations;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Class handling the colony styles messages.
 */
public class ColonyStylesMessage implements IMessage, IMessageHandler<ColonyStylesMessage, IMessage>
{
    private boolean allowPlayerSchematics;
    private Map<String, String> md5Map;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyStylesMessage()
    {
        super();
    }



    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
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
            final String filename = ByteBufUtils.readUTF8String(buf);
            final String md5 = ByteBufUtils.readUTF8String(buf);
            map.put(filename, md5);
        }
        return map;
    }


    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeBoolean(Configurations.allowPlayerSchematics);
        writeMD5MapToByteBuf(buf);
    }

    private static void writeMD5MapToByteBuf(@NotNull final ByteBuf buf)
    {
        Map<String, String> md5s = Structures.getMD5s();
        buf.writeInt(md5s.size());
        for(Map.Entry<String,String> entry : md5s.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            ByteBufUtils.writeUTF8String(buf, entry.getValue());
        }
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
    public IMessage onMessage(@NotNull final ColonyStylesMessage message, final MessageContext ctx)
    {
        Structures.setAllowPlayerSchematics(message.allowPlayerSchematics);
        Structures.setMD5s(message.md5Map);
        return null;
    }
}
