package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Structures;
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
    private Map<String, Integer> hutLevelsMap;
    private Map<String, List<String>> hutStyleMap;
    private Map<String, List<String>> decorationStyleMap;
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
        hutLevelsMap = readHutLevelsMapFromByteBuf(buf);
        hutStyleMap = readStyleMapFromByteBuf(buf);
        decorationStyleMap = readStyleMapFromByteBuf(buf);
        md5Map  = readMD5MapFromByteBuf(buf);
    }

    @NotNull
    private static Map<String, Integer> readHutLevelsMapFromByteBuf(@NotNull final ByteBuf buf)
    {
        @NotNull final Map<String, Integer> map = new HashMap<>();

        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            final String hutName = ByteBufUtils.readUTF8String(buf);
            final Integer hurtMaxLevel = buf.readInt();
            map.put(hutName, hurtMaxLevel);
        }
        return map;
    }

    @NotNull
    private static Map<String, List<String>> readStyleMapFromByteBuf(@NotNull final ByteBuf buf)
    {
        @NotNull final Map<String, List<String>> map = new HashMap<>();

        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            @NotNull final List<String> styles = new ArrayList<>();
            final int numStyles = buf.readInt();
            for (int j = 0; j < numStyles; j++)
            {
                styles.add(ByteBufUtils.readUTF8String(buf));
            }

            map.put(ByteBufUtils.readUTF8String(buf), styles);
        }
        return map;
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
        writeHutLevelsMapToByteBuf(buf);
        writeStyleMapToByteBuf(buf, Structures.getHuts(), Structures::getStylesForHut);
        writeStyleMapToByteBuf(buf, Structures.getDecorations(), Structures::getStylesForDecoration);
        writeMD5MapToByteBuf(buf);
    }

    private static void writeHutLevelsMapToByteBuf(@NotNull final ByteBuf buf)
    {
        Map<String, Integer> hutLevels = Structures.getHutLevels();
        buf.writeInt(hutLevels.size());
        for(Map.Entry<String,Integer> entry : hutLevels.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    private static void writeStyleMapToByteBuf(@NotNull final ByteBuf buf, @NotNull final Set<String> objects, @NotNull final Function<String, List<String>> getStyles)
    {
        buf.writeInt(objects.size());
        for (@NotNull final String object : objects)
        {
            final List<String> styles = getStyles.apply(object);

            buf.writeInt(styles.size());
            for (@NotNull final String style : styles)
            {
                ByteBufUtils.writeUTF8String(buf, style);
            }

            ByteBufUtils.writeUTF8String(buf, object);
        }
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
        Structures.setStyles(message.hutLevelsMap, message.hutStyleMap, message.decorationStyleMap);
        Structures.setMD5s(message.md5Map);
        return null;
    }
}
