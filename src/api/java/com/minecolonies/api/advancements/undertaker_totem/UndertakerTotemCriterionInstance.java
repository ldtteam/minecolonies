package com.minecolonies.api.advancements.undertaker_totem;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * An undertaker recieves a totem of undying criterion instance.
 */
public class UndertakerTotemCriterionInstance extends AbstractCriterionTriggerInstance
{
    public UndertakerTotemCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_UNDERTAKER_TOTEM), ContextAwarePredicate.ANY);
    }
}