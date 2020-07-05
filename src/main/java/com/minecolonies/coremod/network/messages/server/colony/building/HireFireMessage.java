package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class HireFireMessage extends AbstractBuildingServerMessage<IBuildingWorker>
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
    public HireFireMessage(@NotNull final AbstractBuildingView building, final boolean hire, final int citizenID)
    {
        super(building);
        this.hire = hire;
        this.citizenID = citizenID;
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
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuildingWorker building)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCitizen(citizenID);
        citizen.setPaused(false);
        if (hire)
        {

            building.assignCitizen(citizen);
        }
        else
        {
            building.removeCitizen(citizen);
        }
    }
}
