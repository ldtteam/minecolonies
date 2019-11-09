package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewWorkOrderMessage extends AbstractMessage<ColonyViewWorkOrderMessage, IMessage>
{
    private int     colonyId;
    private int     workOrderId;
    private ByteBuf workOrderBuffer;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewWorkOrderMessage()
    {
        super();
    }

    /**
     * Updates a {@link WorkOrderView} of the workOrders.
     *
     * @param colony    colony of the workOrder.
     * @param workOrder workOrder of the colony to update view.
     */
    public ColonyViewWorkOrderMessage(@NotNull final Colony colony, @NotNull final IWorkOrder workOrder)
    {
        this.colonyId = colony.getID();
        this.workOrderBuffer = Unpooled.buffer();
        this.workOrderId = workOrder.getID();
        workOrder.serializeViewNetworkData(workOrderBuffer);
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final ByteBuf newbuf = buf.retain();
        colonyId = newbuf.readInt();
        workOrderId = newbuf.readInt();
        workOrderBuffer = newbuf;
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeBytes(workOrderBuffer);
    }

    @Override
    protected void messageOnClientThread(final ColonyViewWorkOrderMessage message, final MessageContext ctx)
    {
        IColonyManager.getInstance().handleColonyViewWorkOrderMessage(message.colonyId, message.workOrderBuffer, Minecraft.getMinecraft().world.provider.getDimension());
        message.workOrderBuffer.release();
    }
}


