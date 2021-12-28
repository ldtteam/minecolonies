package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The Courier module for the warehouse.
 */
public class CourierAssignmentModule extends AbstractAssignedCitizenModule implements IAssignsJob, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode hiringMode = HiringMode.DEFAULT;

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // If we have no active worker, grab one from the Colony
        if (!isFull() && (this.hiringMode == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.hiringMode == HiringMode.AUTO))
        {
            for (final ICitizenData data : colony.getCitizenManager().getCitizens())
            {
                if (data.getJob() instanceof JobDeliveryman && !hasAssignedCitizen(data) && ((JobDeliveryman) data.getJob()).findWareHouse() == null)
                {
                    assignCitizen(data);
                }
            }
        }

        for (final ICitizenData citizenData : new ArrayList<>(getAssignedCitizen()))
        {
            if (!(citizenData.getJob() instanceof JobDeliveryman))
            {
                removeCitizen(citizenData);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT jobCompound = compound.getCompound("warehouse");
        this.hiringMode = HiringMode.values()[jobCompound.getInt(TAG_HIRING_MODE)];
        final int[] residentIds = jobCompound.getIntArray(TAG_COURIERS);
        for (final int citizenId : residentIds)
        {
            final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
            if (citizen != null)
            {
                assignCitizen(citizen);
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT jobCompound = new CompoundNBT();
        jobCompound.putInt(TAG_HIRING_MODE, this.hiringMode.ordinal());
        if (!assignedCitizen.isEmpty())
        {
            final int[] residentIds = new int[assignedCitizen.size()];
            for (int i = 0; i < assignedCitizen.size(); ++i)
            {
                residentIds[i] = assignedCitizen.get(i).getId();
            }
            jobCompound.putIntArray(TAG_COURIERS, residentIds);
        }
        compound.put("warehouse", jobCompound);
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
        return this.building.getBuildingLevel() * 2;
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.delivery;
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
}
