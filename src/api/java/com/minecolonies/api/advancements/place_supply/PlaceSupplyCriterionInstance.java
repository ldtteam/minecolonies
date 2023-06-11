package com.minecolonies.api.advancements.place_supply;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * A default instance for the "place_supply" trigger, as the conditions are handled in events
 */
public class PlaceSupplyCriterionInstance extends AbstractCriterionTriggerInstance
{
    public PlaceSupplyCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_SUPPLY_PLACED), ContextAwarePredicate.ANY);
    }
}
