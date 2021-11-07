package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class HireFireMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * If hiring (true) else firing.
     */
    private boolean hire;

    /**
     * The citizen to hire/fire.
     */
    private int citizenID;

    /**
     * The job entry,
     */
    private JobEntry entry;

    /**
     * Empty public constructor.
     */
    public HireFireMessage()
    {
        super();
    }

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building  view of the building to read data from
     * @param hire      hire or fire the citizens
     * @param citizenID the id of the citizen to fill the job.
     */
    public HireFireMessage(@NotNull final AbstractBuildingView building, final boolean hire, final int citizenID, final JobEntry entry)
    {
        super(building);
        this.hire = hire;
        this.citizenID = citizenID;
        this.entry = entry;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        hire = buf.readBoolean();
        citizenID = buf.readInt();
        entry = buf.readRegistryId();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(hire);
        buf.writeInt(citizenID);
        buf.writeRegistryId(entry);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);
        citizen.setPaused(false);
        if (hire)
        {
            building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry().equals(entry)).assignCitizen(citizen);
        }
        else
        {
            building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry().equals(entry)).removeCitizen(citizen);
        }
    }
}
