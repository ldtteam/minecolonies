package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.core.colony.buildings.workerbuildings.BuildingKitchen;
import com.minecolonies.core.colony.jobs.JobChef;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts food related things.
 */
public class EntityAIWorkChef extends AbstractEntityAIRequestSmelter<JobChef, BuildingKitchen>
{
    /**
     * Initialize the Cook Assistant.
     *
     * @param cookAssistant the job he has.
     */
    public EntityAIWorkChef(@NotNull final JobChef cookAssistant)
    {
        super(cookAssistant);
    }

    @Override
    public Class<BuildingKitchen> getExpectedBuildingClass()
    {
        return BuildingKitchen.class;
    }
}
