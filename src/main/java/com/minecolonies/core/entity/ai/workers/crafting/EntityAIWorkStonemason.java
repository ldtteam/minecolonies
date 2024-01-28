package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStonemason;
import com.minecolonies.core.colony.jobs.JobStonemason;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts stone related block when needed.
 */
public class EntityAIWorkStonemason extends AbstractEntityAICrafting<JobStonemason, BuildingStonemason>
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

    @Override
    public Class<BuildingStonemason> getExpectedBuildingClass()
    {
        return BuildingStonemason.class;
    }
}
