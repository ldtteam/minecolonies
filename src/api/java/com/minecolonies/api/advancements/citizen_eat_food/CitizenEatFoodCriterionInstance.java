package com.minecolonies.api.advancements.citizen_eat_food;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CitizenEatFoodCriterionInstance extends AbstractCriterionInstance
{
    private ItemPredicate[] itemPredicates;

    public CitizenEatFoodCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD));
    }

    public CitizenEatFoodCriterionInstance(final ItemPredicate[] itemPredicates)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD));

        this.itemPredicates = itemPredicates;
    }

    public boolean test(final ItemStack foodItemStack)
    {
        if (this.itemPredicates != null)
        {
            for (ItemPredicate itemPredicate : itemPredicates)
            {
                if (itemPredicate.test(foodItemStack))
                {
                    return true;
                }
            }
            return false;
        }

        return true;
    }
}
