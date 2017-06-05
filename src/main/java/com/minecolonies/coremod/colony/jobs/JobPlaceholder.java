package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.colony.CitizenData;
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
    public JobPlaceholder(final CitizenData entity)
    {
        super(entity);
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
