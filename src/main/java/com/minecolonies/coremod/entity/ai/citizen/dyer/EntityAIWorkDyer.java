package com.minecolonies.coremod.entity.ai.citizen.dyer;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDyer;
import com.minecolonies.coremod.colony.jobs.JobDyer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIRequestSmelter;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts dye related things.
 */
public class EntityAIWorkDyer extends AbstractEntityAIRequestSmelter<JobDyer, BuildingDyer>
{
    /**
     * Initialize the dyer.
     *
     * @param dyer the job he has.
     */
    public EntityAIWorkDyer(@NotNull final JobDyer dyer)
    {
        super(dyer);
    }

    @Override
    public Class<BuildingDyer> getExpectedBuildingClass()
    {
        return BuildingDyer.class;
    }
}
