package com.minecolonies.coremod.entity.ai.citizen.florist;

import com.minecolonies.coremod.colony.jobs.JobFlorist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

//Todo: implement this class
public class EntityAIWorkFlorist extends AbstractEntityAIInteract<JobFlorist>
{
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkFlorist(@NotNull final JobFlorist job)
    {
        super(job);
    }
}
