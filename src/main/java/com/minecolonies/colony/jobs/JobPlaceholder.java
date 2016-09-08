package com.minecolonies.colony.jobs;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JobPlaceholder extends AbstractJob
{
    public JobPlaceholder(CitizenData entity)
    {
        super(entity);
    }

    @Nonnull
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
    @Nullable
    @Override
    public AbstractAISkeleton generateAI()
    {
        return null;
    }
}
