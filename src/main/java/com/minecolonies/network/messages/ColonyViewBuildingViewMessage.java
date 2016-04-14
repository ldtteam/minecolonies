package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.util.ChunkCoordUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a Building.View to a ColonyView on the client
 */
public class ColonyViewBuildingViewMessage implements IMessage, IMessageHandler<ColonyViewBuildingViewMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;
    private ByteBuf          buildingData;

    public ColonyViewBuildingViewMessage(){}

    /**
     * Creates a
     * @param building      Building to add or update a view for
     */
    public ColonyViewBuildingViewMessage(Building building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = Unpooled.buffer();
        building.serializeToView(this.buildingData);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeBytes(buildingData);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        buildingData = buf;
    }

    @Override
    public IMessage onMessage(ColonyViewBuildingViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewMessage(message.colonyId, message.buildingId, message.buildingData);
    }
}
