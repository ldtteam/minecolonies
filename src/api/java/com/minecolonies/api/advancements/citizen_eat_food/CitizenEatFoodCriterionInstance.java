package com.minecolonies.api.advancements.citizen_eat_food;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/**
 * The test instance to check the "items" condition for the "citizen_eat_food" trigger
 */
public class CitizenEatFoodCriterionInstance extends AbstractCriterionTriggerInstance
{
    private ItemPredicate[] itemPredicates;

    public CitizenEatFoodCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), EntityPredicate.Composite.ANY);
    }

    /**
     * Construct the check with a single item condition
     * @param itemPredicates the food item that has to be eaten to succeed
     */
    public CitizenEatFoodCriterionInstance(final ItemPredicate[] itemPredicates)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), EntityPredicate.Composite.ANY);

        this.itemPredicates = itemPredicates;
    }

    /**
     * Performs the check for the conditions
     * @param foodItemStack the stack of food that was just consumed
     * @return whether the check succeeded
     */
    public boolean test(final ItemStack foodItemStack)
    {
        if (this.itemPredicates != null)
        {
            for (ItemPredicate itemPredicate : itemPredicates)
            {
                if (itemPredicate.matches(foodItemStack))
                {
                    return true;
                }
            }
            return false;
        }

        return true;
    }
}
