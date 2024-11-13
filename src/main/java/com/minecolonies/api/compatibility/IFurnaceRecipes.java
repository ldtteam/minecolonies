package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Interface for the new furnace recipes.
 */
public interface IFurnaceRecipes
{
    /**
     * Get the smelting result for a certain itemStack.
     *
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    ItemStack getSmeltingResult(final ItemStack itemStack);

    /**
     * Get the first smelting recipe by result for a certain itemStorage.
     *
     * @param storage the itemStorage to test.
     * @return the result or null if not existent.
     */
    IRecipeStorage getFirstSmeltingRecipeByResult(final ItemStorage storage);
}
