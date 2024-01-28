package com.minecolonies.api.advancements.max_fields;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class MaxFieldsCriterionInstance extends AbstractCriterionTriggerInstance
{
    public MaxFieldsCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_MAX_FIELDS), ContextAwarePredicate.ANY);
    }
}
