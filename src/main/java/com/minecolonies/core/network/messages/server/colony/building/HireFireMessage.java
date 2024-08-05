package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class HireFireMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "hire_fire", HireFireMessage::new);

    /**
     * If hiring (true) else firing.
     */
    private final boolean hire;

    /**
     * The citizen to hire/fire.
     */
    private final int citizenID;

    /**
     * The module id
     */
    private final int moduleId;

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building  view of the building to read data from
     * @param hire      hire or fire the citizens
     * @param citizenID the id of the citizen to fill the job.
     */
    public HireFireMessage(@NotNull final IBuildingView building, final boolean hire, final int citizenID, final int moduleId)
    {
        super(TYPE, building);
        this.hire = hire;
        this.citizenID = citizenID;
        this.moduleId = moduleId;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected HireFireMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
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
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeBoolean(hire);
        buf.writeInt(citizenID);
        buf.writeInt(moduleId);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getModule(moduleId) instanceof final IAssignsJob module)
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
