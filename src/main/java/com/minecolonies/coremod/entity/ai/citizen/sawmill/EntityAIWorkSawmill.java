package com.minecolonies.coremod.entity.ai.citizen.sawmill;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSawmill;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkSawmill extends AbstractEntityAICrafting<JobSawmill, BuildingSawmill>
{
    /**
     * Initialize the sawmill and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkSawmill(@NotNull final JobSawmill sawmill)
    {
        super(sawmill);
    }

    @Override
    public Class<BuildingSawmill> getExpectedBuildingClass()
    {
        return BuildingSawmill.class;
    }
}
