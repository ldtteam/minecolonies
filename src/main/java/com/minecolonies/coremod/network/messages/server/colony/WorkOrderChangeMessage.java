package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the WorkOrderChangeMessage which is responsible for changes in priority or removal of workOrders.
 */
public class WorkOrderChangeMessage extends AbstractColonyServerMessage
{
    /**
     * The workOrder to remove or change priority.
     */
    private final int workOrderId;

    /**
     * The priority.
     */
    private final int priority;

    /**
     * Remove the workOrder or not.
     */
    private final boolean removeWorkOrder;

    /**
     * Empty public constructor.
     */
    public WorkOrderChangeMessage(final PacketBuffer buf)
    {
        super(buf);
        this.workOrderId = buf.readInt();
        this.priority = buf.readInt();
        this.removeWorkOrder = buf.readBoolean();
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
        super(building.getColony());
        this.workOrderId = workOrderId;
        this.removeWorkOrder = removeWorkOrder;
        this.priority = priority;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(workOrderId);
        buf.writeInt(priority);
        buf.writeBoolean(removeWorkOrder);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (removeWorkOrder)
        {
            colony.getWorkManager().removeWorkOrder(workOrderId);
        }
        else if (colony.getWorkManager().getWorkOrder(workOrderId) != null)
        {
            colony.getWorkManager().getWorkOrder(workOrderId).setPriority(priority);
        }
    }
}
