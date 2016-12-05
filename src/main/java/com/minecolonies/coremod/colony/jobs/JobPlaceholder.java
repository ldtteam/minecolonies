package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JobPlaceholder extends AbstractJob
{
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
