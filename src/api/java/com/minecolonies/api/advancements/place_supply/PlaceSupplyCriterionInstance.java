package com.minecolonies.api.advancements.place_supply;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public class PlaceSupplyCriterionInstance extends AbstractCriterionInstance
{
    public PlaceSupplyCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_SUPPLY_PLACED));
    }
}
