package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Send a message to the server to mark the building as dirty.
 * Created: January 20, 2017
 *
 * @author xavierh
 */
public class MarkBuildingDirtyMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos buildingId;
    /**
     * The id of the colony.
     */
    private int      colonyId;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty constructor used when registering the 
     */
    public MarkBuildingDirtyMessage()
    {
        super();
    }

    /**
     * Creates a mark building dirty request 
     *
     * @param building AbstractBuilding of the request.
     */
    public MarkBuildingDirtyMessage(@NotNull final AbstractBuildingView building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
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
        if (colony == null)
        {
            Log.getLogger().warn("MarkBuildingDirtyMessage colony is null");
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (building == null || building.getTileEntity() == null)
        {
            Log.getLogger().warn("MarkBuildingDirtyMessage building or tileEntity is null");
            return;
        }

        building.getTileEntity().markDirty();
    }
}
