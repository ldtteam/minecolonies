package com.minecolonies.api.advancements.deep_mine;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class DeepMineCriterionInstance extends AbstractCriterionTriggerInstance
{
    public DeepMineCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_DEEP_MINE), ContextAwarePredicate.ANY);
    }
}
