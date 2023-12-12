package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
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
     * The module id
     */
    private int moduleId;

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
    public HireFireMessage(@NotNull final IBuildingView building, final boolean hire, final int citizenID, final int moduleId)
    {
        super(building);
        this.hire = hire;
        this.citizenID = citizenID;
        this.moduleId = moduleId;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        hire = buf.readBoolean();
        citizenID = buf.readInt();
        moduleId = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(hire);
        buf.writeInt(citizenID);
        buf.writeInt(moduleId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(moduleId) instanceof IAssignsJob module)
        {
            final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);
            citizen.setPaused(false);
            if (hire)
            {
                module.assignCitizen(citizen);
            }
            else
            {
                module.removeCitizen(citizen);
            }
        }
    }
}
