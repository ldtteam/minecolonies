package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class of the placeholder job.
 * Used if a certain building doesn't have a job yet.
 */
public class JobPlaceholder extends AbstractJob
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobPlaceholder(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.placeHolder;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Placeholder";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public AbstractAISkeleton<JobPlaceholder> generateAI()
    {
        return null;
    }
}
