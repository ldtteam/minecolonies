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
import com.minecolonies.coremod.colony.jobs.JobQuarryMiner;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_HIRING_MODE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MINERS;

/**
 * The Courier module for the warehouse.
 */
public class QuarryModule extends AbstractAssignedCitizenModule implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode hiringMode = HiringMode.DEFAULT;

    /**
     * If the quarry was finished.
     */
    private boolean isFinished = false;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && (this.hiringMode == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.hiringMode == HiringMode.AUTO))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.getJob() instanceof JobQuarryMiner && !hasAssignedCitizen(data) && ((JobQuarryMiner) data.getJob()).findQuarry() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData : new ArrayList<>(getAssignedCitizen()))
        {
            if (!(citizenData.getJob() instanceof JobQuarryMiner))
            {
                removeCitizen(citizenData);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT quarryCompound = compound.getCompound("quarryassignment");
        this.hiringMode = HiringMode.values()[quarryCompound.getInt(TAG_HIRING_MODE)];
        final int[] residentIds = quarryCompound.getIntArray(TAG_MINERS);
        for (final int citizenId : residentIds)
        {
            final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
            if (citizen != null)
            {
                assignCitizen(citizen);
            }
        }
        this.isFinished = quarryCompound.getBoolean("isfinished");
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT quarrycompound = new CompoundNBT();
        quarrycompound.putInt(TAG_HIRING_MODE, this.hiringMode.ordinal());
        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            quarrycompound.putIntArray(TAG_MINERS, residentIds);
        }
        compound.put("quarryassignment", quarrycompound);
        compound.putBoolean("isfinished", isFinished);
    }

    @Override
    public void serializeToView(final @NotNull PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeInt(hiringMode.ordinal());
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
        return ModJobs.quarryMiner;
    }

    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        this.markDirty();
    }

    public HiringMode getHiringMode()
    {
        return hiringMode;
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
