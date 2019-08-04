package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which manages the messages assigning or unassigning of citizens.
 */
public class AssignUnassignMessage implements IMessage
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * The buildings position.
     */
    private BlockPos buildingId;

    /**
     * If assigning (true) else unassigning.
     */
    private boolean assign;

    /**
     * The citizen to assign/unassigning.
     */
    private int citizenID;

    /**
     * The dimension of the
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public AssignUnassignMessage()
    {
        super();
    }

    /**
     * Creates object for the player to assigning or unassigning a citizen.
     *
     * @param building  view of the building to read data from
     * @param assign    assign or unassigning the citizens
     * @param citizenID the id of the citizen to fill the job.
     */
    public AssignUnassignMessage(@NotNull final IBuildingView building, final boolean assign, final int citizenID)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.citizenID = citizenID;
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
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        assign = buf.readBoolean();
        citizenID = buf.readInt();
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
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeBoolean(assign);
        buf.writeInt(citizenID);
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
        final ServerPlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);

            if (!(building instanceof BuildingHome))
            {
                return;
            }

            final ICitizenData citizen = colony.getCitizenManager().getCitizen(citizenID);
            if (assign && !building.isFull() && !building.equals(citizen.getHomeBuilding()))
            {
                if (citizen.getHomeBuilding() != null)
                {
                    citizen.getHomeBuilding().removeCitizen(citizen);
                }
                building.assignCitizen(citizen);
            }
            else if (building.hasAssignedCitizen(citizen))
            {
                building.removeCitizen(citizen);
            }
        }
    }
}
