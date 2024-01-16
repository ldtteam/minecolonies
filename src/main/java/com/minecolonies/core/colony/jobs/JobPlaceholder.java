package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.basic.AbstractAISkeleton;
import org.jetbrains.annotations.Nullable;

/**
 * Class of the placeholder job. Used if a certain building doesn't have a job yet.
 */
public class JobPlaceholder extends AbstractJob<AbstractAISkeleton<JobPlaceholder>, JobPlaceholder>
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
