package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinerSetLevelMessage implements IMessage, IMessageHandler<MinerSetLevelMessage, IMessage>
{
    private int              colonyId;
    private BlockPos         buildingId;
    private int              level;

    public MinerSetLevelMessage(){}

    /**
     * Creates object for the miner set level message
     *
     * @param building       View of the building to read data from
     * @param level          Level of the miner
     */
    public MinerSetLevelMessage(BuildingMiner.View building, int level)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.level = level;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(level);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
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
