package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewWorkOrderMessage implements IMessage
{
    private int     colonyId;
    private int     workOrderId;
    private PacketBuffer workOrderBuffer;

    /**
     * Empty constructor used when registering the 
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        final PacketBuffer newbuf = buf.retain();
        colonyId = newbuf.readInt();
        workOrderId = newbuf.readInt();
        workOrderBuffer = newbuf;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
        buf.writeBytes(workOrderBuffer);
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
        IColonyManager.getInstance().handleColonyViewWorkOrderMessage(colonyId, workOrderBuffer, Minecraft.getInstance().world.getDimension().getType().getId());
    }
}


