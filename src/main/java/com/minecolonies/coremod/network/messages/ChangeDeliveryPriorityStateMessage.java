package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.Stash;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChangeDeliveryPriorityStateMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * Id of the building.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public ChangeDeliveryPriorityStateMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the delivery.
     *
     * @param building view of the building to read data from
     */
    public ChangeDeliveryPriorityStateMessage(@NotNull final AbstractBuildingView building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getPosition();
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(this.buildingId);
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
        final PlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);

            if (building instanceof AbstractBuildingWorker || building instanceof Stash)
            {
                building.alterPriorityState();
                building.markDirty();
            }
        }
    }
}
