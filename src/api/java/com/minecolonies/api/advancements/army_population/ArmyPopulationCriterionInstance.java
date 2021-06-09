package com.minecolonies.api.advancements.army_population;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * The test instance to check the "population_count" for the "army_population" trigger
 */
public class ArmyPopulationCriterionInstance extends CriterionInstance
{
    private final int populationCount;

    /**
     * Registers a deserialized advancement trigger with this criterion condition
     * @param populationCount the current army size
     */
    public ArmyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ARMY_POPULATION), EntityPredicate.AndPredicate.ANY);

        this.populationCount = populationCount;
    }

    /**
     * Perform the check comparing the current population to the advancement condition
     * @param populationCount the current population
     * @return whether the check passed
     */
    public boolean test(final int populationCount)
    {
        // Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }
}
