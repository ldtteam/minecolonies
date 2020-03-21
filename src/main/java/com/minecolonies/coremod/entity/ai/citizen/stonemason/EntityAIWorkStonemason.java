package com.minecolonies.coremod.entity.ai.citizen.stonemason;

import com.minecolonies.coremod.colony.jobs.JobStonemason;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts stone related block when needed.
 */
public class EntityAIWorkStonemason extends AbstractEntityAICrafting<JobStonemason>
{
    /**
     * Initialize the Stonemason and add all his tasks.
     *
     * @param stonemason the job he has.
     */
    public EntityAIWorkStonemason(@NotNull final JobStonemason stonemason)
    {
        super(stonemason);
    }
}
