package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.core.colony.buildings.workerbuildings.BuildingKitchen;
import com.minecolonies.core.colony.jobs.JobChef;

import static com.minecolonies.api.util.constant.StatisticsConstants.ITEMS_BAKED_DETAIL;
import static com.minecolonies.api.util.constant.StatisticsConstants.FOOD_COOKED_DETAIL;

import org.jetbrains.annotations.NotNull;

/**
 * Crafts food related things.
 */
public class EntityAIWorkChef extends AbstractEntityAIRequestSmelter<JobChef, BuildingKitchen>
{
    /**
     * Initialize the Chef.
     *
     * @param jobChef the job he has.
     */
    public EntityAIWorkChef(@NotNull final JobChef jobChef)
    {
        super(jobChef);
    }

    @Override
    public Class<BuildingKitchen> getExpectedBuildingClass()
    {
        return BuildingKitchen.class;
    }

    /**
     * Returns the name of the smelting stat that is used in the building's statistics.
     * @return the name of the smelting stat.
     */
    protected String getSmeltingStatName()
    {
        return ITEMS_BAKED_DETAIL;
    }

    /**
     * Returns the name of the crafting stat that is used in the building's statistics.
     * Override this in your subclass to change the description of the smelting stat.
     * @return The name of the crafting statistic.
     */
    protected String getCraftingStatName()
    {
        return FOOD_COOKED_DETAIL;
    }
}
