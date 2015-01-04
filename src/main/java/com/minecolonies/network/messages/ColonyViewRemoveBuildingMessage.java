package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.network.PacketUtils;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveBuildingMessage implements IMessage, IMessageHandler<ColonyViewRemoveBuildingMessage, IMessage>
{
    private UUID             colonyId;
    private ChunkCoordinates buildingId;

    public ColonyViewRemoveBuildingMessage(){}

    public ColonyViewRemoveBuildingMessage(Colony colony, ChunkCoordinates building)
    {
        this.colonyId = colony.getID();
        this.buildingId = building;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketUtils.writeUUID(buf, colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = PacketUtils.readUUID(buf);
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveBuildingMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveBuildingMessage(message.colonyId, message.buildingId);
    }
}
