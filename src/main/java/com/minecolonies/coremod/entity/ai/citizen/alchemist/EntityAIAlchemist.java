package com.minecolonies.coremod.entity.ai.citizen.alchemist;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingAlchemist;
import com.minecolonies.coremod.colony.jobs.JobAlchemist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

//TODO
public class EntityAIAlchemist extends AbstractEntityAIInteract<JobAlchemist, BuildingAlchemist>
{
    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIAlchemist(@NotNull final JobAlchemist job)
    {
        super(job);
    }

    /**
     * Can be overridden in implementations to return the exact building type the worker expects.
     *
     * @return the building type associated with this AI's worker.
     */
    @Override
    public Class<BuildingAlchemist> getExpectedBuildingClass()
    {
        return BuildingAlchemist.class;
    }
}
