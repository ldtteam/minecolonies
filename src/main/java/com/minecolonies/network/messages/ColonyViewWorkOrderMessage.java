package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.workorders.AbstractWorkOrder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewWorkOrderMessage implements IMessage, IMessageHandler<ColonyViewWorkOrderMessage, IMessage>
{
    private int     colonyId;
    private int     workOrderId;
    private ByteBuf workOrderBuffer;

    /**
     * Empty public constructor.
    */
    public ColonyViewWorkOrderMessage()
    {
        /**
         * Intentionally left empty.
         **/
    }

    /**
     * Updates a {@link com.minecolonies.colony.WorkOrderView} of the workOrders.
     *
     * @param colony  colony of the workOrder.
     * @param workOrder workOrder of the colony to update view.
     */
    public ColonyViewWorkOrderMessage(Colony colony, AbstractWorkOrder workOrder)
    {
        this.colonyId = colony.getID();
        this.workOrderBuffer = Unpooled.buffer();
        this.workOrderId = workOrder.getID();
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
    public IMessage onMessage(ColonyViewWorkOrderMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewWorkOrderMessage(message.colonyId, message.workOrderBuffer);
    }
}


