package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.IJobView;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IJobDataManager
{

    static IJobDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobDataManager();
    }

    /**
     * Create a Job from saved CompoundTag data.
     *
     * @param citizen  The citizen that owns the Job.
     * @param compound The CompoundTag containing the saved Job data.
     * @return New Job created from the data, or null.
     */
    @Nullable
    IJob<?> createFrom(ICitizenData citizen, @NotNull CompoundTag compound, @NotNull final HolderLookup.Provider provider);

    /**
     * Create a job view from the saved network buffer.
     * @param colony the colony.
     * @param citizenDataView the the citizen data view..
     * @param networkBuffer the buffer/
     * @return the new job view.
     */
    IJobView createViewFrom(final IColonyView colony, final ICitizenDataView citizenDataView, final RegistryFriendlyByteBuf networkBuffer);
}
