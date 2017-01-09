package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the WorkOrderChangeMessage which is responsible for changes in priority or removal of workOrders.
 */
public class WorkOrderChangeMessage extends AbstractMessage<WorkOrderChangeMessage, IMessage>
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
    public WorkOrderChangeMessage(@NotNull final AbstractBuilding.View building, final int workOrderId, final boolean removeWorkOrder, final int priority)
    {
        super();
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
    public void fromBytes(@NotNull final ByteBuf buf)
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
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeInt(priority);
        buf.writeBoolean(removeWorkOrder);
    }

    @Override
    public void messageOnServerThread(final WorkOrderChangeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null && colony.getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS))
        {
            final boolean hasPermission = colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS);
            if (!hasPermission)
            {
                return;
            }

            if (message.removeWorkOrder)
            {
                removeWorkOrder(colony, message);
            }
            else
            {
                colony.getWorkManager().getWorkOrder(message.workOrderId).setPriority(message.priority);
            }
        }
    }

    private void removeWorkOrder(final Colony colony, final WorkOrderChangeMessage message)
    {
        WorkOrderBuild orderBuild = colony.getWorkManager().getWorkOrder(message.workOrderId, WorkOrderBuild.class);
        if (orderBuild == null)
        {
            colony.getWorkManager().removeWorkOrder(message.workOrderId);
            return;
        }
        CitizenData citizen = colony.getCitizen(orderBuild.getClaimedBy());
        if (citizen == null)
        {
            colony.getWorkManager().removeWorkOrder(message.workOrderId);
            return;
        }
        final JobBuilder job = citizen.getJob(JobBuilder.class);
        if (job != null)
        {
            job.cancelCurrentJob();
        }
    }
}


