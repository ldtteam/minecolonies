package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the level of the miner from the GUI.
 */
public class MinerSetLevelMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private int      level;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty constructor used when registering the 
     */
    public MinerSetLevelMessage()
    {
        super();
    }

    /**
     * Creates object for the miner set level 
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
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        level = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(level);
        buf.writeInt(dimension);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingMiner building = colony.getBuildingManager().getBuilding(buildingId, BuildingMiner.class);
            if (building != null && level >= 0 && level < building.getNumberOfLevels())
            {
                building.setCurrentLevel(level);
            }
        }
    }
}
