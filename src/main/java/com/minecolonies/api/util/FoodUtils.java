package com.minecolonies.api.util;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.SATURATION;
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
        return foodProperties != null && foodProperties.nutrition() >= buildingLevel + 1;
    }

    /**
     * Calculate the given max building level for a given food.
     * @param resource the stack to check.
     * @return the building level.
     */
    public static int getBuildingLevelForFood(final ItemStack resource)
    {
        return Math.max(2, Math.min(resource.getFoodProperties(null).nutrition() - 1, MAX_BUILDING_LEVEL));
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param itemFood the food properties of that food.
     * @param housingLevel the citizen's current housing level.
     * @param researchBonus the bonus from research (0 for no bonus).
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, @Nullable final FoodProperties itemFood, final int housingLevel, final double researchBonus)
    {
        if (itemFood == null)
        {
            return 0;
        }

        final double saturationNerf = getFoodTier(foodStack) > 0 ? 1.0 : (1.0 / (housingLevel + 1));
        return itemFood.nutrition() * saturationNerf * (1.0 + researchBonus) / 2.0;
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param citizen the citizen consuming the food.
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, final AbstractEntityCitizen citizen)
    {
        final FoodProperties itemFood = foodStack.getItem().getFoodProperties(foodStack, citizen);
        final int housingLevel = citizen.getCitizenData().getHomeBuilding() == null ? 0 : citizen.getCitizenData().getHomeBuilding().getBuildingLevel();
        final double researchBonus = citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SATURATION);
        return getFoodValue(foodStack, itemFood, housingLevel, researchBonus);
    }

    /**
     * @param stack The food item stack.
     * @return The food tier, in the range [0, 3].
     */
    public static int getFoodTier(ItemStack stack)
    {
        if (stack.is(ModTags.tier3food)) return 3;
        if (stack.is(ModTags.tier2food)) return 2;
        if (stack.is(ModTags.tier1food)) return 1;
        return 0;
    }
}
