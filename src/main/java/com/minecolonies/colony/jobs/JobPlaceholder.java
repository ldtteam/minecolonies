package com.minecolonies.colony.jobs;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;

public class JobPlaceholder extends Job
{
    public JobPlaceholder(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Placeholder";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return null;
    }
}
