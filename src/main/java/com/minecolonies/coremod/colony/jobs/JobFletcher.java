package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.fletcher.EntityAIWorkFletcher;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Fletcher job.
 */
public class JobFletcher extends AbstractJobCrafter
{
    /**
     * Instantiates the job for the Fletcher.
     *
     * @param entity the citizen who becomes a Fletcher
     */
    public JobFletcher(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.fletcher;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.fletcher";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobFletcher> generateAI()
    {
        return new EntityAIWorkFletcher(this);
    }
}
