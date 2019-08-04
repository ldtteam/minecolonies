package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveWorkOrderMessage implements IMessage
{

    private int colonyId;
    private int workOrderId;

    /**
     * Empty constructor used when registering the message.
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

    @Override
    protected void messageOnClientThread(final ColonyViewRemoveWorkOrderMessage message, final MessageContext ctx)
    {
        IColonyManager.getInstance().handleColonyViewRemoveWorkOrderMessage(message.colonyId, message.workOrderId, Minecraft.getMinecraft().world.world.getDimension().getType().getId());
    }
}
