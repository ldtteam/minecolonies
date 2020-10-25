package com.minecolonies.coremod.entity.ai.citizen.fletcher;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFletcher;
import com.minecolonies.coremod.colony.jobs.JobFletcher;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts wood related block when needed.
 */
public class EntityAIWorkFletcher extends AbstractEntityAICrafting<JobFletcher, BuildingFletcher>
{
    /**
     * Initialize the fletcher and add all his tasks.
     *
     * @param fletcher the job he has.
     */
    public EntityAIWorkFletcher(@NotNull final JobFletcher fletcher)
    {
        super(fletcher);
    }

    @Override
    public Class<BuildingFletcher> getExpectedBuildingClass()
    {
        return BuildingFletcher.class;
    }
}
