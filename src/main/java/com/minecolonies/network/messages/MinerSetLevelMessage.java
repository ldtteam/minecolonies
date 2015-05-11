package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class MinerSetLevelMessage implements IMessage, IMessageHandler<MinerSetLevelMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;
    private int              mode;

    public MinerSetLevelMessage(){}

    public MinerSetLevelMessage(Building.View building, int mode)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        mode = buf.readInt();
    }

    @Override
    public IMessage onMessage(MinerSetLevelMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            BuildingMiner building = colony.getBuilding(message.buildingId, BuildingMiner.class);
            if (building != null)
            {
                building.currentLevel = message.mode;
            }
        }
        return null;
    }
}
