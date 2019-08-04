package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Creates the WorkOrderChangeMessage which is responsible for changes in priority or removal of workOrders.
 */
public class WorkOrderChangeMessage implements IMessage
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
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public WorkOrderChangeMessage()
    {
        super();
    }

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building        view of the building to read data from
     * @param workOrderId     the workOrderId.
     * @param removeWorkOrder remove the workOrder?
     * @param priority        the new priority.
     */
    public WorkOrderChangeMessage(@NotNull final IBuildingView building, final int workOrderId, final boolean removeWorkOrder, final int priority)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.workOrderId = workOrderId;
        this.removeWorkOrder = removeWorkOrder;
        this.priority = priority;
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
        priority = buf.readInt();
        removeWorkOrder = buf.readBoolean();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeInt(priority);
        buf.writeBoolean(removeWorkOrder);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final WorkOrderChangeMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
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
    }
}


