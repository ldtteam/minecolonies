package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.IJobView;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
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
    IJob<?> createFrom(ICitizenData citizen, @NotNull CompoundNBT compound);

    /**
     * Create a job view from the saved network buffer.
     * @param colony the colony.
     * @param citizenDataView the the citizen data view..
     * @param networkBuffer the buffer/
     * @return the new job view.
     */
    IJobView createViewFrom(final IColonyView colony, final ICitizenDataView citizenDataView, final PacketBuffer networkBuffer);
}
