package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.permissions.Permissions;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Creates the WorkOrderChangeMessage which is responsible for changes in priority or removal of workOrders.
 */
public class WorkOrderChangeMessage implements IMessage, IMessageHandler<WorkOrderChangeMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * The workOrder to remove or change priority.
     */
    private int workOrderId;

    /**
     * The priority.
     */
    private int priority;

    /**
     * Remove the workOrder or not.
     */
    private boolean removeWorkOrder;

    /**
     * Empty public constructor.
     */
    public WorkOrderChangeMessage()
    {
        /**
         * Intentionally left empty.
         **/
    }

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building        view of the building to read data from
     * @param workOrderId     the workOrderId.
     * @param removeWorkOrder remove the workOrder?
     * @param priority        the new priority.
     */
    public WorkOrderChangeMessage(AbstractBuilding.View building, int workOrderId, boolean removeWorkOrder, int priority)
    {
        this.colonyId = building.getColony().getID();
        this.workOrderId = workOrderId;
        this.removeWorkOrder = removeWorkOrder;
        this.priority = priority;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
        priority = buf.readInt();
        removeWorkOrder = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeInt(priority);
        buf.writeBoolean(removeWorkOrder);
    }

    /**
     * Called when a message has been received.
     *
     * @param message the message.
     * @param ctx     the context.
     * @return possible response, in this case it is null.
     */
    @Override
    public IMessage onMessage(WorkOrderChangeMessage message, MessageContext ctx)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null && colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.ACCESS_HUTS))
        {
            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.ACCESS_HUTS))
            {
                return null;
            }

            if (message.removeWorkOrder)
            {
                colony.getWorkManager().removeWorkOrder(message.workOrderId);
            }
            else
            {
                colony.getWorkManager().getWorkOrder(message.workOrderId).setPriority(message.priority);
            }
        }
        return null;
    }
}


