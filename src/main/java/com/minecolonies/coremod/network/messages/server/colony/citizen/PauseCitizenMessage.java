package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class PauseCitizenMessage extends AbstractColonyServerMessage
{
    /**
     * The citizen to pause.
     */
    private int citizenID;

    /**
     * Empty public constructor.
     */
    public PauseCitizenMessage()
    {
        super();
    }

    /**
     * Creates object for the player to switch pause state of a citizen.
     *
     * @param building  view of the building to read data from
     * @param citizenID the id of the citizen to fill the job.
     */
    public PauseCitizenMessage(@NotNull final AbstractBuildingView building, final int citizenID)
    {
        super(building.getColony());
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

        buf.writeInt(citizenID);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCitizen(citizenID);
        citizen.setPaused(!citizen.isPaused());
    }
}
