package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveCitizenMessage implements IMessage, IMessageHandler<ColonyViewRemoveCitizenMessage, IMessage>
{
    private int colonyId;
    private int citizenId;

    public ColonyViewRemoveCitizenMessage(){}

    public ColonyViewRemoveCitizenMessage(Colony colony, int citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveCitizenMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveCitizenMessage(message.colonyId, message.citizenId);
    }
}
