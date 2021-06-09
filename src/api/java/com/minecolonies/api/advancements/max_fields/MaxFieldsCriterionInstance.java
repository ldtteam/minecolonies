package com.minecolonies.api.advancements.max_fields;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class MaxFieldsCriterionInstance extends CriterionInstance
{
    public MaxFieldsCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_MAX_FIELDS), EntityPredicate.AndPredicate.ANY);
    }
}
