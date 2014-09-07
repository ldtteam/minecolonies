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
 * Add or Update a Building.View to a ColonyView on the client
 */
public class ColonyBuildingViewMessage implements IMessage
{
    private UUID           colonyId;
    private NBTTagCompound building;

    public ColonyBuildingViewMessage(){}

    public ColonyBuildingViewMessage(UUID colonyId, NBTTagCompound building)
    {
        this.colonyId = colonyId;
        this.building = building;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(colonyId.getMostSignificantBits());
        buf.writeLong(colonyId.getLeastSignificantBits());
        ByteBufUtils.writeTag(buf, building);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = new UUID(buf.readLong(), buf.readLong());
        building = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<ColonyBuildingViewMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ColonyBuildingViewMessage message, MessageContext ctx)
        {
            return ColonyManager.handleColonyBuildingViewPacket(message.colonyId, message.building);
        }
    }
}
