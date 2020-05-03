package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for removing a view on the client, used for cleaning up after deletion
 */
public class ColonyViewRemoveMessage implements IMessage
{
    private int id;
    private int dimension;

    public ColonyViewRemoveMessage()
    {
        super();
    }

    public ColonyViewRemoveMessage(final int id, final int dimension)
    {
        this.id = id;
        this.dimension = dimension;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(id);
        buf.writeInt(dimension);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        id = buf.readInt();
        dimension = buf.readInt();
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
