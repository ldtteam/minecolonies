package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.stonemason.EntityAIWorkStonemason;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Stonemason job.
 */
public class JobStonemason extends AbstractJobCrafter<EntityAIWorkStonemason, JobStonemason>
{
    /**
     * Instantiates the job for the Stonemason.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobStonemason(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkStonemason generateAI()
    {
        return new EntityAIWorkStonemason(this);
    }
}
