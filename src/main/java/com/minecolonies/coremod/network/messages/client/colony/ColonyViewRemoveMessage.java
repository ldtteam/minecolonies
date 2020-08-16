package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for removing a view on the client, used for cleaning up after deletion
 */
public class ColonyViewRemoveMessage implements IMessage
{
    private final int id;
    private final ResourceLocation dimension;

    public ColonyViewRemoveMessage(final PacketBuffer buf)
    {
        this.id = buf.readInt();
        this.dimension = new ResourceLocation(buf.readString(32767));
    }

    public ColonyViewRemoveMessage(final int id, final ResourceLocation dimension)
    {
        this.id = id;
        this.dimension = dimension;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(id);
        buf.writeString(dimension.toString());
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
        IColonyManager.getInstance().removeColonyView(id, dimension);
    }
}
