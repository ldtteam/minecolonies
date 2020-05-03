package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingQuarry;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class AssignQuarryStationMessage implements IMessage
{
    @Nullable
    private BlockPos stationId;
    private int dimensionId;
    private int colonyId;
    private BlockPos buildingId;

    /**
     * Empty standard constructor.
     */
    public AssignQuarryStationMessage()
    {
    }

    /**
     * Network message for assigning stations to a quarry;
     *
     * @param stationId the ID of the station we're adding to our building.
     */
    public AssignQuarryStationMessage(final int dimensionId, final int colonyId, final BlockPos buildingId, @Nullable final BlockPos stationId)
    {
        super();
        this.dimensionId = dimensionId;
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.stationId = stationId;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(dimensionId);
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);

        buf.writeBoolean(stationId != null);
        if (stationId != null)
        {
            buf.writeBlockPos(stationId);
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.dimensionId = buf.readInt();
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        if (buf.readBoolean())
        {
            this.stationId = buf.readBlockPos();
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimensionId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingQuarry building = colony.getBuildingManager().getBuilding(buildingId, BuildingQuarry.class);
            if (building != null)
            {
                building.setStationPos(this.stationId);
            }
        }
    }
}
