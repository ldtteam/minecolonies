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
    private Map<String, String> md5Map;
    private Map<String, Map<String, Map<String, String>>> schematicsMap;

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
        md5Map = readMD5MapFromByteBuf(buf);
        schematicsMap  = readSchematicMapFromByteBuf(buf);
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

    @NotNull
    private static Map<String, Map<String, Map<String, String>>> readSchematicMapFromByteBuf(@NotNull final ByteBuf buf)
    {
        @NotNull final Map<String, Map<String, Map<String, String>>> map = new HashMap<>();


        final int countSection = buf.readInt();
        for (int iSection = 0; iSection < countSection; iSection++)
        {
            final String section = ByteBufUtils.readUTF8String(buf);
            final Map<String, Map<String, String>> sectionMap = new HashMap<>();
            final int countStyle = buf.readInt();
            for (int iStyle = 0; iStyle < countStyle; iStyle++)
            {
                final String style = ByteBufUtils.readUTF8String(buf);
                final Map<String, String> sMap = new HashMap<>();
                final int countSchematic = buf.readInt();
                for (int iSchematic = 0; iSchematic < countSchematic; iSchematic++)
                {
                    sMap.put(ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf));
                }
                sectionMap.put(style, sMap);
            }
            map.put(section, sectionMap);
        }
        return map;
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        writeMD5MapToByteBuf(buf);
        writeSchematicMapToByteBuf(buf);
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

    private static void writeSchematicMapToByteBuf(@NotNull final ByteBuf buf)
    {
        Map<String, Map<String, Map<String, String>>> schematics = Structures.getSchematics();

        buf.writeInt(schematics.size());
        for(Map.Entry<String, Map<String, Map<String, String>>> section : schematics.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, section.getKey());
            buf.writeInt(section.getValue().size());

            for(Map.Entry<String, Map<String, String>> style : section.getValue().entrySet())
            {
                ByteBufUtils.writeUTF8String(buf, style.getKey());
                buf.writeInt(style.getValue().size());
                for(Map.Entry<String, String> schematic : style.getValue().entrySet())
                {
                    ByteBufUtils.writeUTF8String(buf, schematic.getKey());
                    ByteBufUtils.writeUTF8String(buf, schematic.getValue());
                }
            }
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
        Structures.setMD5s(message.md5Map);
        Structures.setSchematics(message.schematicsMap);
        return null;
    }
}
