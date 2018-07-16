package com.minecolonies.coremod.entity.ai.citizen.composter;

import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

public class EntityAIWorkComposter extends AbstractEntityAIInteract<JobComposter>
{
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkComposter(@NotNull final JobComposter job)
    {
        super(job);
    }
}
