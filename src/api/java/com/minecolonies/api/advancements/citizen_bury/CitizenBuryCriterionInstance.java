package com.minecolonies.api.advancements.citizen_bury;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * A citizen is buried criterion instance.
 */
public class CitizenBuryCriterionInstance extends CriterionInstance
{
    public CitizenBuryCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_BURY), EntityPredicate.AndPredicate.ANY);
    }
}
