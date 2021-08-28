package com.minecolonies.api.advancements.colony_population;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * The test instance to check the "population_count" for the "colony_population" trigger
 */
public class ColonyPopulationCriterionInstance extends AbstractCriterionTriggerInstance
{
    private int populationCount;

    /**
     * Constructs a check with a single condition
     * @param populationCount the population that has to be reached to succeed
     */
    public ColonyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COLONY_POPULATION), EntityPredicate.Composite.ANY);

        this.populationCount = populationCount;
    }

    /**
     * Performs the check for the conditions
     * @param populationCount the current population
     * @return whether the check succeeded
     */
    public boolean test(final int populationCount)
    {
        //Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }
}
