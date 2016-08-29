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
public class ColonyViewRemoveCitizenMessage implements IMessage, IMessageHandler<ColonyViewRemoveCitizenMessage, IMessage>
{
    private int colonyId;
    private int citizenId;

    public ColonyViewRemoveCitizenMessage() {}

    /**
     * Creates an object for the remove message for citizen
     *
     * @param colony  Colony the citizen is in
     * @param citizen Citizen ID
     */
    public ColonyViewRemoveCitizenMessage(Colony colony, int citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveCitizenMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveCitizenMessage(message.colonyId, message.citizenId);
    }
}
