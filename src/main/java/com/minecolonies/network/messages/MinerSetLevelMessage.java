package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.ChunkCoordUtils;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class MinerSetLevelMessage implements IMessage, IMessageHandler<MinerSetLevelMessage, IMessage>
{
    private int              colonyId;
    private ChunkCoordinates buildingId;
    private int              level;

    public MinerSetLevelMessage(){}

    /**
     * Creates object for the miner set level message
     *
     * @param building       View of the building to read data from
     * @param level          Level of the miner
     */
    public MinerSetLevelMessage(Building.View building, int level)  //TODO don't we want BuildingMiner.View ?
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.level = level;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ChunkCoordUtils.writeToByteBuf(buf, buildingId);
        buf.writeInt(level);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = ChunkCoordUtils.readFromByteBuf(buf);
        level = buf.readInt();
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
                if(message.level >= 0 && message.level < building.getLevels().size())
                {
                    building.currentLevel = message.level;
                }
            }
        }
        return null;
    }
}
