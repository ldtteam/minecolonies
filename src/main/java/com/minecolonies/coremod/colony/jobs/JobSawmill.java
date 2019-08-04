package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.sawmill.EntityAIWorkSawmill;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Sawmill job.
 */
public class JobSawmill extends AbstractJobCrafter
{
    /**
     * Instantiates the job for the Sawmill.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobSawmill(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Sawmill";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobSawmill> generateAI()
    {
        return new EntityAIWorkSawmill(this);
    }
}
