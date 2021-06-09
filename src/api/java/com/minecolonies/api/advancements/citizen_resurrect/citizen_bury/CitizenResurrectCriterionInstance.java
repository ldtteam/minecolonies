package com.minecolonies.api.advancements.citizen_resurrect.citizen_bury;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * A citizen is buried criterion instance.
 */
public class CitizenResurrectCriterionInstance extends CriterionInstance
{
    public CitizenResurrectCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_RESURRECT), EntityPredicate.AndPredicate.ANY);
    }
}
