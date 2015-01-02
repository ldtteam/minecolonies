package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChunkCoordinates;

import java.util.UUID;

/**
 * Add or Update a Building.View to a ColonyView on the client
 */
public class ColonyViewBuildingViewMessage implements IMessage, IMessageHandler<ColonyViewBuildingViewMessage, IMessage>
{
    private UUID             colonyId;
    private ChunkCoordinates buildingId;
    private PacketBuffer     buildingData = new PacketBuffer(Unpooled.buffer());

    public ColonyViewBuildingViewMessage(){}

    public ColonyViewBuildingViewMessage(Building building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        building.serializeToView(this.buildingData);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeBytes(buildingData);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public IMessage onMessage(ColonyViewBuildingViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewMessage(message.colonyId, message.buildingId, message.buildingData);
    }
}
