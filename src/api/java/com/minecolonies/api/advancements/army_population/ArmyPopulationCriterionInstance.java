package com.minecolonies.api.advancements.army_population;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.util.ResourceLocation;

public class ArmyPopulationCriterionInstance extends CriterionInstance
{
    private int populationCount;

    public ArmyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ARMY_POPULATION));

        this.populationCount = populationCount;
    }

    public boolean test(final int populationCount)
    {
        //Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }
}
