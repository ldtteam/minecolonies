package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewCitizensMessage implements IMessage
{
    private UUID colonyId;
    private UUID citizenId;
    private NBTTagCompound citizen;

    public ColonyViewCitizensMessage(){}

    public ColonyViewCitizensMessage(UUID colonyId, UUID citizenId, NBTTagCompound citizen)
    {
        this.colonyId = colonyId;
        this.citizenId = citizenId;
        this.citizen = citizen;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(colonyId.getMostSignificantBits());
        buf.writeLong(colonyId.getLeastSignificantBits());
        buf.writeLong(citizenId.getMostSignificantBits());
        buf.writeLong(citizenId.getLeastSignificantBits());
        ByteBufUtils.writeTag(buf, citizen);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = new UUID(buf.readLong(), buf.readLong());
        citizenId = new UUID(buf.readLong(), buf.readLong());
        citizen = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<ColonyViewCitizensMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ColonyViewCitizensMessage message, MessageContext ctx)
        {
            return ColonyManager.handleColonyViewCitizensPacket(message.colonyId, message.citizenId, message.citizen);
        }
    }
}
