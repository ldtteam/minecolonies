package com.minecolonies.core.network.messages.server.colony.building.home;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.core.colony.buildings.modules.AbstractAssignedCitizenModule;
import com.minecolonies.core.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the messages assigning or unassigning of citizens.
 */
public class AssignUnassignMessage extends AbstractBuildingServerMessage<DefaultBuildingInstance>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "assign_unassign", AssignUnassignMessage::new);

    /**
     * If assigning (true) else unassigning.
     */
    private final boolean assign;

    /**
     * The citizen to assign/unassigning.
     */
    private final int citizenID;

    /**
     * The job entry.
     */
    private final JobEntry jobEntry;

    /**
     * Creates object for the player to assigning or unassigning a citizen.
     *
     * @param building  view of the building to read data from
     * @param assign    assign or unassigning the citizens
     * @param citizenID the id of the citizen to fill the job.
     * @param entry the job entry.
     */
    public AssignUnassignMessage(@NotNull final IBuildingView building, final boolean assign, final int citizenID, final JobEntry entry)
    {
        super(TYPE, building);
        this.assign = assign;
        this.citizenID = citizenID;
        this.jobEntry = entry;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    protected AssignUnassignMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        assign = buf.readBoolean();
        citizenID = buf.readInt();
        jobEntry = buf.readBoolean() ? buf.readById(IMinecoloniesAPI.getInstance().getJobRegistry()::byIdOrThrow) : null;
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
        buf.writeBoolean(assign);
        buf.writeInt(citizenID);
        buf.writeBoolean(jobEntry != null);
        if (jobEntry != null)
        {
            buf.writeById(IMinecoloniesAPI.getInstance().getJobRegistry()::getIdOrThrow, jobEntry);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final DefaultBuildingInstance building)
    {
        final ICitizenData citizen = colony.getCitizenManager().getCivilian(citizenID);
        final AbstractAssignedCitizenModule module;
        if (jobEntry == null)
        {
            module = building.getFirstModuleOccurance(LivingBuildingModule.class);
        }
        else
        {
            module = building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry);
        }

        if (assign && !module.isFull() && !building.equals(citizen.getHomeBuilding()))
        {
            if (citizen.getHomeBuilding() != null)
            {
                citizen.getHomeBuilding().getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(citizen);
            }
            module.assignCitizen(citizen);
        }
        else if (module.hasAssignedCitizen(citizen))
        {
            module.removeCitizen(citizen);
        }
    }
}
