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
    private Map<String, List<String>> hutStyleMap;
    private Map<String, List<String>> decorationStyleMap;

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
        hutStyleMap = readStyleMapFromByteBuf(buf);
        decorationStyleMap = readStyleMapFromByteBuf(buf);
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

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        writeStyleMapToByteBuf(buf, Structures.getHuts(), Structures::getStylesForHut);
        writeStyleMapToByteBuf(buf, Structures.getDecorations(), Structures::getStylesForDecoration);
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
        Structures.setStyles(message.hutStyleMap, message.decorationStyleMap);
        return null;
    }
}
