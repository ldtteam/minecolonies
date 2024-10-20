package com.minecolonies.api.util;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

import java.util.Set;

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

        final double saturationNerf = foodStack.getItem() instanceof IMinecoloniesFoodItem ? 1.0 : (1.0 / (housingLevel + 1));
        return itemFood.getNutrition() * saturationNerf * (1.0 + researchBonus) / 2.0;
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
     * Get the best food for a given citizen from a given inventory and return the index where it is.
     * @param inventoryCitizen the inventory to check.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched.
     * @return the matching inv slot, or -1.
     */
    public static int getBestFoodForCitizen(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, final Set<ItemStorage> menu)
    {
        // Smaller score is better.
        int bestScore = Integer.MAX_VALUE;
        int bestSlot = -1;
        for (int i = 0; i < inventoryCitizen.getSlots(); i++)
        {
            final ItemStorage invStack = new ItemStorage(inventoryCitizen.getStackInSlot(i));
            if (menu.contains(invStack) && (citizenData.getHomeBuilding() == null || FoodUtils.canEat(invStack.getItemStack(), citizenData.getHomeBuilding().getBuildingLevel())))
            {
                final int localScore = citizenData.checkLastEaten(invStack.getItem());
                if (localScore < bestScore)
                {
                    if (localScore < 0)
                    {
                        return i;
                    }
                    bestScore = localScore;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }
}
