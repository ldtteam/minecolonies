package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.UUID;

/**
 * Add or Update a Building.View to a ColonyView on the client
 */
public class ColonyBuildingViewMessage implements IMessage, IMessageHandler<ColonyBuildingViewMessage, IMessage>
{
    private UUID             colonyId;
    private ChunkCoordinates buildingId;
    private NBTTagCompound   building;

    public ColonyBuildingViewMessage(){}

    public ColonyBuildingViewMessage(UUID colonyId, ChunkCoordinates buildingId, NBTTagCompound building)
    {
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.building = building;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(colonyId.getMostSignificantBits());
        buf.writeLong(colonyId.getLeastSignificantBits());
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        ByteBufUtils.writeTag(buf, building);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = new UUID(buf.readLong(), buf.readLong());
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        building = ByteBufUtils.readTag(buf);
    }

    @Override
    public IMessage onMessage(ColonyBuildingViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewPacket(message.colonyId, message.buildingId, message.building);
    }
}
