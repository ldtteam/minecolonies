package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

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
     * The dimension of the message.
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
        buildingId = BlockPosUtil.readFromByteBuf(buf);
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
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assign);
        buf.writeInt(citizenID);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final AssignUnassignMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);

            if (!(building instanceof BuildingHome))
            {
                return;
            }

            final ICitizenData citizen = colony.getCitizenManager().getCitizen(message.citizenID);
            if (message.assign && !building.isFull() && !building.equals(citizen.getHomeBuilding()))
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
