package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.util.ChunkCoordUtils;

import io.netty.buffer.ByteBuf;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.util.ChunkCoordinates;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveBuildingMessage implements IMessage, IMessageHandler<ColonyViewRemoveBuildingMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;

    public ColonyViewRemoveBuildingMessage(){}

    /**
     * Creates an object for the building remove message
     *
     * @param colony        Colony the building is in
     * @param building      Building that is removed
     */
    public ColonyViewRemoveBuildingMessage(Colony colony, ChunkCoordinates building)
    {
        this.colonyId = colony.getID();
        this.buildingId = building;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveBuildingMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveBuildingMessage(message.colonyId, message.buildingId);
    }
}
