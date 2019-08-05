package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IJobDataManager
{

    static IJobDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobDataManager();
    }

    /**
     * Create a Job from saved CompoundNBT data.
     *
     * @param citizen  The citizen that owns the Job.
     * @param compound The CompoundNBT containing the saved Job data.
     * @return New Job created from the data, or null.
     */
    @Nullable
    IJob createFrom(ICitizenData citizen, @NotNull CompoundNBT compound);
}
