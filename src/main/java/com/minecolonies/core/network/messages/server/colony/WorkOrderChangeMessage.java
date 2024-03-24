package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the WorkOrderChangeMessage which is responsible for changes in priority or removal of workOrders.
 */
public class WorkOrderChangeMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "work_order_change", WorkOrderChangeMessage::new);

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
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building        view of the building to read data from
     * @param workOrderId     the workOrderId.
     * @param removeWorkOrder remove the workOrder?
     * @param priority        the new priority.
     */
    public WorkOrderChangeMessage(@NotNull final IBuildingView building, final int workOrderId, final boolean removeWorkOrder, final int priority)
    {
        super(TYPE, building.getColony());
        this.workOrderId = workOrderId;
        this.removeWorkOrder = removeWorkOrder;
        this.priority = priority;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected WorkOrderChangeMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
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
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(workOrderId);
        buf.writeInt(priority);
        buf.writeBoolean(removeWorkOrder);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
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


