package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the level of the miner from the GUI.
 */
public class MinerSetLevelMessage extends AbstractMessage<MinerSetLevelMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private int      level;

    /**
     * Empty constructor used when registering the message.
     */
    public MinerSetLevelMessage()
    {
        super();
    }

    /**
     * Creates object for the miner set level message.
     *
     * @param building View of the building to read data from.
     * @param level    Level of the miner.
     */
    public MinerSetLevelMessage(@NotNull final BuildingMiner.View building, final int level)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(level);
    }

    @Override
    public void messageOnServerThread(final MinerSetLevelMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingMiner building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingMiner.class);
            if (building != null && message.level >= 0 && message.level < building.getNumberOfLevels())
            {
                building.setCurrentLevel(message.level);
            }
        }
    }
}
