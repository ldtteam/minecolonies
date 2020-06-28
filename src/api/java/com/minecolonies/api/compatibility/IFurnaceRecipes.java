package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.item.ItemStack;

import java.util.Map;

/**
 * Interface for the new furnace recipes.
 */
public interface IFurnaceRecipes
{
    /**
     * Set the map.
     * This is called from the client side message.
     * @param map the map to set.
     */
    void setMap(final Map<ItemStorage, RecipeStorage> map);

    /**
     * Get the smelting result for a certain itemStack.
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    ItemStack getSmeltingResult(final ItemStack itemStack);
}
