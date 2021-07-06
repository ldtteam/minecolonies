package com.minecolonies.api.advancements.all_towers;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class AllTowersCriterionInstance extends CriterionInstance
{
    public AllTowersCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ALL_TOWERS), EntityPredicate.AndPredicate.ANY);
    }
}
