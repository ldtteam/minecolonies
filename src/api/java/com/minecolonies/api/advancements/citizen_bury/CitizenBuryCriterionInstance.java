package com.minecolonies.api.advancements.citizen_bury;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * A citizen is buried criterion instance.
 */
public class CitizenBuryCriterionInstance extends AbstractCriterionTriggerInstance
{
    public CitizenBuryCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_BURY), ContextAwarePredicate.ANY);
    }
}
