package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.jobs.JobQuarrier;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The main data module for the quarry.
 */
public class QuarryModule extends AbstractAssignedCitizenModule implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    /**
     * If the quarry was finished.
     */
    private boolean isFinished = false;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && (this.getHiringMode() == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.getHiringMode() == HiringMode.AUTO))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.getJob() instanceof JobQuarrier && !hasAssignedCitizen(data) && ((JobQuarrier) data.getJob()).findQuarry() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData : new ArrayList<>(getAssignedCitizen()))
        {
            if (!(citizenData.getJob() instanceof JobQuarrier))
            {
                removeCitizen(citizenData);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        final CompoundTag quarryCompound = compound.getCompound(TAG_QUARRY_ASSIGNMENT);
        final int[] residentIds = quarryCompound.getIntArray(TAG_MINERS);
        for (final int citizenId : residentIds)
        {
            final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
            if (citizen != null)
            {
                assignCitizen(citizen);
            }
        }
        this.isFinished = quarryCompound.getBoolean(TAG_IS_FINISHED);
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        super.serializeNBT(compound);

        final CompoundTag quarrycompound = new CompoundTag();
        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            quarrycompound.putIntArray(TAG_MINERS, residentIds);
        }
        compound.put(TAG_QUARRY_ASSIGNMENT, quarrycompound);
        quarrycompound.putBoolean(TAG_IS_FINISHED, isFinished);
    }

    @Override
    public void onRemoval(final ICitizenData citizen)
    {

    }

    @Override
    public void onAssignment(final ICitizenData citizen)
    {

    }

    @Override
    public int getModuleMax()
    {
        return 1;
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.quarrier;
    }

    /**
     * Check if the quarry was completed already.
     * @return true if so.
     */
    public boolean isFinished()
    {
        return isFinished;
    }

    /**
     * Set the quarry as finished.
     */
    public void setFinished()
    {
        isFinished = true;
        markDirty();
    }
}
