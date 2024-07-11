package com.minecolonies.api.util;

import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.world.item.ItemStack;

public class FoodUtils
{
    public static boolean canEat(final ItemStack stack, IBuilding building)
    {
        return building.getBuildingLevel() < 3 || stack.getItem().getFoodProperties(stack, null).getNutrition() >= building.getBuildingLevel() + 1;
    }
}
