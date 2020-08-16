package com.minecolonies.coremod.network.messages.server.colony.building.home;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages assigning or unassigning of citizens.
 */
public class AssignUnassignMessage extends AbstractBuildingServerMessage<BuildingHome>
{
    /**
     * If assigning (true) else unassigning.
     */
    private final boolean assign;

    /**
     * The citizen to assign/unassigning.
     */
    private final int citizenID;

    /**
     * Empty public constructor.
     */
    public AssignUnassignMessage(final PacketBuffer buf)
    {
        super(buf);
        this.assign = buf.readBoolean();
        this.citizenID = buf.readInt();
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
        super(building);
        this.assign = assign;
        this.citizenID = citizenID;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(assign);
        buf.writeInt(citizenID);
    }

    @Override
    public boolean errorIfCastFails()
    {
        return false;
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingHome building)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);
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
