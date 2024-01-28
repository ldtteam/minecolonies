package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStoneSmeltery;
import com.minecolonies.core.colony.jobs.JobStoneSmeltery;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAIRequestSmelter;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts furnace stone related block when needed.
 */
public class EntityAIWorkStoneSmeltery extends AbstractEntityAIRequestSmelter<JobStoneSmeltery, BuildingStoneSmeltery>
{
    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param jobStoneSmeltery the job he has.
     */
    public EntityAIWorkStoneSmeltery(@NotNull final JobStoneSmeltery jobStoneSmeltery)
    {
        super(jobStoneSmeltery);
    }

    @Override
    public Class<BuildingStoneSmeltery> getExpectedBuildingClass()
    {
        return BuildingStoneSmeltery.class;
    }
}
