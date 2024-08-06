package com.minecolonies.core.colony.jobs.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JobDataManager implements IJobDataManager
{
    @Nullable
    @Override
    public IJob<?> createFrom(final ICitizenData citizen, @NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        final ResourceLocation jobType =
          compound.contains(NbtTagConstants.TAG_JOB_TYPE) ? ResourceLocation.parse(compound.getString(NbtTagConstants.TAG_JOB_TYPE)) : ModJobs.PLACEHOLDER_ID;
        final IJob<?> job = IJobRegistry.getInstance().get(jobType).produceJob(citizen);

        if (job != null)
        {
            try
            {
                job.deserializeNBT(provider, compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("A Job %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  jobType), ex);
                return null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", jobType));
        }

        return job;
    }

    @Override
    public IJobView createViewFrom(
      final IColonyView colony, final ICitizenDataView citizenDataView, final RegistryFriendlyByteBuf networkBuffer)
    {
        final ResourceLocation jobName = ResourceLocation.parse(networkBuffer.readUtf(32767));
        final JobEntry entry = IJobRegistry.getInstance().get(jobName);

        if (entry == null)
        {
            Log.getLogger().error(String.format("Unknown job type '%s'.", jobName), new Exception());
            return null;
        }

        final IJobView view = entry.getJobViewProducer().get().apply(colony, citizenDataView);

        if (view != null)
        {
            view.deserialize(networkBuffer);
        }

        return view;
    }
}
