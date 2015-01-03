package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.network.PacketUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveCitizenMessage implements IMessage, IMessageHandler<ColonyViewRemoveCitizenMessage, IMessage>
{
    private UUID colonyId;
    private UUID citizenId;

    public ColonyViewRemoveCitizenMessage(){}

    public ColonyViewRemoveCitizenMessage(Colony colony, UUID citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        PacketUtils.writeUUID(buf, citizenId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        citizenId = PacketUtils.readUUID(buf);
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveCitizenMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveCitizenMessage(message.colonyId, message.citizenId);
    }
}
