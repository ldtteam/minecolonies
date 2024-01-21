package com.minecolonies.api.advancements.citizen_resurrect;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * A citizen is buried criterion instance.
 */
public class CitizenResurrectCriterionInstance extends AbstractCriterionTriggerInstance
{
    public CitizenResurrectCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_RESURRECT), ContextAwarePredicate.ANY);
    }
}
