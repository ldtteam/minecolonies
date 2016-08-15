package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.WorkOrderView;
import com.minecolonies.colony.workorders.AbstractWorkOrder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewWorkOrderMessage implements IMessage, IMessageHandler<com.minecolonies.network.messages.ColonyViewWorkOrderMessage, IMessage>
{
    private int     colonyId;
    private int     workOrderId;
    private ByteBuf workOrderBuffer;

    public ColonyViewWorkOrderMessage(){}

    /**
     * Updates a {@link com.minecolonies.colony.WorkOrderView} of the workOrders
     *
     * @param colony  Colony of the citizen
     * @param workOrder Workorder of the colony to update view
     */
    public ColonyViewWorkOrderMessage(Colony colony, AbstractWorkOrder workOrder)
    {
        this.colonyId = colony.getID();
        this.workOrderId = workOrder.getID();
        this.workOrderBuffer = Unpooled.buffer();
        workOrder.serializeViewNetworkData(workOrderBuffer);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeBytes(workOrderBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
        workOrderBuffer = buf;
    }

    @Override
    public IMessage onMessage(com.minecolonies.network.messages.ColonyViewWorkOrderMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewWorkOrderMessage(message.colonyId, message.workOrderBuffer);
    }
}


