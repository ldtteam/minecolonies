package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.Colony;

import net.minecraft.client.Minecraft;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveWorkOrderMessage implements IMessage
{

    private int colonyId;
    private int workOrderId;

    /**
     * Empty constructor used when registering the 
     */
    public ColonyViewRemoveWorkOrderMessage()
    {
        super();
    }

    /**
     * Creates an object for the remove message for citizen.
     *
     * @param colony      colony the workOrder is in.
     * @param workOrderId workOrder ID.
     */
    public ColonyViewRemoveWorkOrderMessage(@NotNull final Colony colony, final int workOrderId)
    {
        this.colonyId = colony.getID();
        this.workOrderId = workOrderId;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
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
        IColonyManager.getInstance().handleColonyViewRemoveWorkOrderMessage(colonyId, workOrderId, Minecraft.getInstance().world.getDimension().getType().getId());
    }
}
