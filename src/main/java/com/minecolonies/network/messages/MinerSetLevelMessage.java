package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinerSetLevelMessage implements IMessage, IMessageHandler<MinerSetLevelMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private int      level;

    public MinerSetLevelMessage() {}

    /**
     * Creates object for the miner set level message
     *
     * @param building View of the building to read data from
     * @param level    Level of the miner
     */
    public MinerSetLevelMessage(@NotNull BuildingMiner.View building, int level)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(level);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull MinerSetLevelMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            @Nullable BuildingMiner building = colony.getBuilding(message.buildingId, BuildingMiner.class);
            if (building != null)
            {
                if (message.level >= 0 && message.level < building.getNumberOfLevels())
                {
                    building.setCurrentLevel(message.level);
                }
            }
        }
        return null;
    }
}
