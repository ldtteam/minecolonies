package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the hiring mode of a building.
 */
public class BuildingHiringModeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The Hiring mode to set.
     */
    private HiringMode mode;

    /**
     * The job id.
     */
    private String jobId;

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
    public BuildingHiringModeMessage(@NotNull final IBuildingView building, final HiringMode mode, final String jobId)
    {
        super(building);
        this.mode = mode;
        this.jobId = jobId;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        mode = HiringMode.values()[buf.readInt()];
        jobId = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(mode.ordinal());
        buf.writeUtf(jobId);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobID().equals(jobId)).setHiringMode(mode);
    }
}
