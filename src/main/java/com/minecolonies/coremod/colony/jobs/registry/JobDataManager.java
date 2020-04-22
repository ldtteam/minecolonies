package com.minecolonies.coremod.colony.jobs.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JobDataManager implements IJobDataManager
{
    @Nullable
    @Override
    public IJob createFrom(
      final ICitizenData citizen, @NotNull final CompoundNBT compound)
    {
        final ResourceLocation jobType =
          compound.keySet().contains(NbtTagConstants.TAG_JOB_TYPE) ? new ResourceLocation(compound.getString(NbtTagConstants.TAG_JOB_TYPE)) : ModJobs.PLACEHOLDER_ID;
        final IJob job = IJobRegistry.getInstance().getValue(jobType).getHandlerProducer().apply(citizen);

        if (job != null)
        {
            try
            {
                job.deserializeNBT(compound);
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
}
