package com.minecolonies.coremod.network.messages;

import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Empty constructor used when registering the 
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
    private static Map<String, String> readMD5MapFromByteBuf(@NotNull final PacketBuffer buf)
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
        buf.writeBoolean(MineColonies.getConfig().getCommon().allowPlayerSchematics.get());
        writeMD5MapToByteBuf(buf);
    }

    private static void writeMD5MapToByteBuf(@NotNull final PacketBuffer buf)
    {
        final Map<String, String> md5s = Structures.getMD5s();
        buf.writeInt(md5s.size());
        for (final Map.Entry<String, String> entry : md5s.entrySet())
        {
            buf.writeString(entry.getKey());
            buf.writeString(entry.getValue());
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        Structures.setAllowPlayerSchematics(allowPlayerSchematics);
        Structures.setMD5s(md5Map);
    }
}
