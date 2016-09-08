package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveWorkOrderMessage implements IMessage, IMessageHandler<ColonyViewRemoveWorkOrderMessage, IMessage>
{

    private int colonyId;
    private int workOrderId;

    /**
     * Empty public constructor.
     */
    public ColonyViewRemoveWorkOrderMessage()
    {
        /**
         * Intentionally left empty.
         **/
    }

    /**
     * Creates an object for the remove message for citizen.
     *
     * @param colony      colony the workOrder is in.
     * @param workOrderId workOrder ID.
     */
    public ColonyViewRemoveWorkOrderMessage(@Nonnull Colony colony, int workOrderId)
    {
        this.colonyId = colony.getID();
        this.workOrderId = workOrderId;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf)
    {
        colonyId = buf.readInt();
        workOrderId = buf.readInt();
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(workOrderId);
    }

    @Nullable
    @Override
    public IMessage onMessage(@Nonnull ColonyViewRemoveWorkOrderMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveWorkOrderMessage(message.colonyId, message.workOrderId);
    }
}
