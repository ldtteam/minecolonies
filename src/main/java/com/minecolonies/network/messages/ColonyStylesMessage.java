package com.minecolonies.network.messages;

import com.minecolonies.colony.Schematics;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.*;
import java.util.function.Function;

public class ColonyStylesMessage implements IMessage, IMessageHandler<ColonyStylesMessage, IMessage>
{
    private Map<String, List<String>> hutStyleMap;
    private Map<String, List<String>> decorationStyleMap;

    public ColonyStylesMessage(){}

    @Override
    public void toBytes(ByteBuf buf)
    {
        writeStyleMapToByteBuf(buf, Schematics.getHuts(), Schematics::getStylesForHut);
        writeStyleMapToByteBuf(buf, Schematics.getDecorations(), Schematics::getStylesForDecoration);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        hutStyleMap = readStyleMapFromByteBuf(buf);
        decorationStyleMap = readStyleMapFromByteBuf(buf);
    }

    private void writeStyleMapToByteBuf(ByteBuf buf, Set<String> objects, Function<String, List<String>> getStyles)
    {
        buf.writeInt(objects.size());
        for(String object : objects)
        {
            List<String> styles = getStyles.apply(object);

            buf.writeInt(styles.size());
            for(String style : styles)
            {
                ByteBufUtils.writeUTF8String(buf, style);
            }

            ByteBufUtils.writeUTF8String(buf, object);
        }
    }

    private Map<String, List<String>> readStyleMapFromByteBuf(ByteBuf buf)
    {
        Map<String, List<String>> map = new HashMap<>();

        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            List<String> styles = new ArrayList<>();
            int numStyles = buf.readInt();
            for(int j = 0; j < numStyles; j++)
            {
                styles.add(ByteBufUtils.readUTF8String(buf));
            }

            map.put(ByteBufUtils.readUTF8String(buf), styles);
        }
        return map;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the styles of the huts to the given value in the message
     *
     * @param message       Message
     * @param ctx           Context
     * @return              Null
     */
    @Override
    public IMessage onMessage(ColonyStylesMessage message, MessageContext ctx)
    {
        Schematics.setStyles(message.hutStyleMap, message.decorationStyleMap);
        return null;
    }
}
