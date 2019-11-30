package com.minecolonies.api.advancements.colony_population;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public class ColonyPopulationCriterionInstance extends AbstractCriterionInstance
{
    private int populationCount;

    public ColonyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COLONY_POPULATION));

        this.populationCount = populationCount;
    }

    public boolean test(final int populationCount)
    {
        //Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }
}
