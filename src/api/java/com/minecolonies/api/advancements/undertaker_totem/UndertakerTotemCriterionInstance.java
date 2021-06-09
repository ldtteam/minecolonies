package com.minecolonies.api.advancements.undertaker_totem;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * An undertaker recieves a totem of undying criterion instance.
 */
public class UndertakerTotemCriterionInstance extends CriterionInstance
{
    public UndertakerTotemCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_UNDERTAKER_TOTEM), EntityPredicate.AndPredicate.ANY);
    }
}