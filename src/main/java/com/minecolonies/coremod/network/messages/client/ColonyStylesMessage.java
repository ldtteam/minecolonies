package com.minecolonies.coremod.network.messages.client;

import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Class handling the colony styles messages.
 */
public class ColonyStylesMessage implements IMessage
{
    private final Map<String, String> md5Map;

    /**
     * Empty constructor used when registering the
     */
    public ColonyStylesMessage(final PacketBuffer buf)
    {
        final int count = buf.readInt();
        md5Map = new HashMap<>(count);
        for (int i = 0; i < count; i++)
        {
            final String filename = buf.readString(32767);
            final String md5 = buf.readString(32767);
            md5Map.put(filename, md5);
        }
    }

    public ColonyStylesMessage()
    {
        this.md5Map = Structures.getMD5s();
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(md5Map.size());
        for (final Map.Entry<String, String> entry : md5Map.entrySet())
        {
            buf.writeString(entry.getKey());
            buf.writeString(entry.getValue());
        }
    }

    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        Structures.setMD5s(md5Map);
    }
}
