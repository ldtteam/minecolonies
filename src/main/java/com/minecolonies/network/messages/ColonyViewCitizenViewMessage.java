package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.network.PacketUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewCitizenViewMessage implements IMessage, IMessageHandler<ColonyViewCitizenViewMessage, IMessage>
{
    private UUID colonyId;
    private UUID citizenId;
    private PacketBuffer citizenBuffer = new PacketBuffer(Unpooled.buffer());

    public ColonyViewCitizenViewMessage(){}

    public ColonyViewCitizenViewMessage(Colony colony, CitizenData citizen) throws IOException
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen.getId();
        citizen.serializeViewNetworkData(citizenBuffer);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        PacketUtils.writeUUID(buf, citizenId);
        buf.writeBytes(citizenBuffer);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        citizenId = PacketUtils.readUUID(buf);
        buf.readBytes(citizenBuffer, buf.readableBytes());
    }

    @Override
    public IMessage onMessage(ColonyViewCitizenViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewCitizensMessage(message.colonyId, message.citizenId, message.citizenBuffer);
    }
}
