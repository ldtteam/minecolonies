package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the hiring mode of a building.
 */
public class BuildingHiringModeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The Hiring mode to set.
     */
    private HiringMode    mode;

    /**
     * The job id.
     */
    private JobEntry jobId;

    /**
     * Check if living building module.
     */
    private boolean isLivingBuildingModule;

    /**
     * Empty constructor used when registering the
     */
    public BuildingHiringModeMessage()
    {
        super();
    }

    /**
     * Creates object for the hiring mode
     *
     * @param building View of the building to read data from.
     * @param mode     the hiring mode.
     */
    public BuildingHiringModeMessage(@NotNull final IBuildingView building, final HiringMode mode, final JobEntry jobId)
    {
        super(building);
        this.mode = mode;
        this.jobId = jobId;
        if (jobId == null)
        {
            this.isLivingBuildingModule = true;
        }
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        mode = HiringMode.values()[buf.readInt()];
        isLivingBuildingModule = buf.readBoolean();
        if (!isLivingBuildingModule)
        {
            this.jobId = buf.readRegistryIdSafe(JobEntry.class);
        }
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(mode.ordinal());
        buf.writeBoolean(isLivingBuildingModule);
        if (jobId != null)
        {
            buf.writeRegistryId(IMinecoloniesAPI.getInstance().getJobRegistry(), jobId);
        }
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (isLivingBuildingModule)
        {
            building.getFirstModuleOccurance(LivingBuildingModule.class).setHiringMode(mode);
            building.getColony().getCitizenManager().calculateMaxCitizens();
        }
        else
        {
            building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobId).setHiringMode(mode);
        }
    }
}
