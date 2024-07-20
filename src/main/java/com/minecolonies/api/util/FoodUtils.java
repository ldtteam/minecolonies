package com.minecolonies.api.util;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * Food specific util functions.
 */
public class FoodUtils
{
    /**
     * Check if that food can be eaten at a given building level.
     * @param stack the stack to check.
     * @param buildingLevel the respective building level.
     * @return true if so.
     */
    public static boolean canEat(final ItemStack stack, final int buildingLevel)
    {
        if (buildingLevel < 3)
        {
            return stack.getItem().getFoodProperties(stack, null) != null;
        }
        final FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        return foodProperties != null && foodProperties.getNutrition() >= buildingLevel + 1;
    }

    /**
     * Calculate the given max building level for a given food.
     * @param resource the stack to check.
     * @return the building level.
     */
    public static int getBuildingLevelForFood(final ItemStack resource)
    {
        return Math.max(2, Math.min(resource.getFoodProperties(null).getNutrition() - 1, MAX_BUILDING_LEVEL));
    }
}
