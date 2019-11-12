package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set add or remove a worker to gather from.
 */
public class EnchanterWorkerSetMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the enchanter.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

    /**
     * The worker to add/remove.
     */
    private BlockPos worker;

    /**
     * true if add, false if remove.
     */
    private boolean add;

    /**
     * Empty constructor used when registering the 
     */
    public EnchanterWorkerSetMessage()
    {
        super();
    }

    /**
     * Create the enchanter worker 
     * @param building the building of the enchanter.
     * @param worker the worker to add/remove.
     * @param add true if add, else false
     */
    public EnchanterWorkerSetMessage(@NotNull final BuildingEnchanter.View building, final BlockPos worker, final boolean add)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.worker = worker;
        this.dimension = building.getColony().getDimension();
        this.add = add;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
        worker = buf.readBlockPos();
        add = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
        buf.writeBlockPos(worker);
        buf.writeBoolean(add);
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

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(ctxIn.getSender(), Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingEnchanter building = colony.getBuildingManager().getBuilding(buildingId, BuildingEnchanter.class);
            if (building != null)
            {
                if (add)
                {
                    building.addWorker(worker);
                }
                else
                {
                    building.removeWorker(worker);
                }
            }
        }
    }
}
