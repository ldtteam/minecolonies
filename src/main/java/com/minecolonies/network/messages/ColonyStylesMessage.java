package com.minecolonies.network.messages;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ColonyStylesMessage implements IMessage, IMessageHandler<ColonyStylesMessage, IMessage>
{
    private Map<String, Set<String>> hutStyleMap;
    public ColonyStylesMessage(){}

    @Override
    public void toBytes(ByteBuf buf)
    {
        Set<String> huts = ColonyManager.getHuts();

        buf.writeInt(huts.size());
        for(String hut : huts)
        {
            Set<String> styles = ColonyManager.getStylesForHut(hut);

            buf.writeInt(styles.size());
            for(String style : styles)
            {
                ByteBufUtils.writeUTF8String(buf, style);
            }

            ByteBufUtils.writeUTF8String(buf, hut);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        hutStyleMap = new HashMap<String, Set<String>>();

        for(int i = 0; i < buf.readInt(); i++)
        {
            Set<String> styles = new HashSet<String>();
            for(int j = 0; j < buf.readInt(); j++)
            {
                styles.add(ByteBufUtils.readUTF8String(buf));
            }

            hutStyleMap.put(ByteBufUtils.readUTF8String(buf), styles);
        }
    }

    @Override
    public IMessage onMessage(ColonyStylesMessage message, MessageContext ctx)
    {
        ColonyManager.setStyles(hutStyleMap);
        return null;
    }
}
