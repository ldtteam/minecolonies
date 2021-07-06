package com.minecolonies.api.advancements.deep_mine;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * All towers criterion instance.
 */
public class DeepMineCriterionInstance extends CriterionInstance
{
    public DeepMineCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_DEEP_MINE), EntityPredicate.AndPredicate.ANY);
    }
}
