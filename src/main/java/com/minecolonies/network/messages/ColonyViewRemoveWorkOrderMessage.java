package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveWorkOrderMessage implements IMessage, IMessageHandler<ColonyViewRemoveWorkOrderMessage, IMessage>
{

    private int colonyId;
    private int workOrderId;

    public ColonyViewRemoveWorkOrderMessage(){}

    /**
     * Creates an object for the remove message for citizen
     *
     * @param colony  Colony the citizen is in
     * @param workOrderId Citizen ID
     */
    public ColonyViewRemoveWorkOrderMessage(Colony colony, int workOrderId)
    {
        this.colonyId = colony.getID();
        this.workOrderId = workOrderId;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveWorkOrderMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveWorkOrderMessage(message.colonyId, message.workOrderId);
    }

}
