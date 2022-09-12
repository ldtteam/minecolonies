package com.minecolonies.api.compatibility;

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
     * Get the first smelting recipe by result for a certain itemStack predicate.
     *
     * @param stackPredicate the predicate to test.
     * @return the result or null if not existent.
     */
    public RecipeStorage getFirstSmeltingRecipeByResult(final Predicate<ItemStack> stackPredicate);
}
