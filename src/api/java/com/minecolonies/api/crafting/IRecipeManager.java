package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * The Interface describing the recipeManager which takes care of the recipes discovered by the colonies in this world.
 */
public interface IRecipeManager
{
    /**
     * Get a unmodifiable copy of the recipes map.
     * @return a map of Token, RecipeStorage.
     */
    ImmutableMap<IToken, IRecipeStorage> getRecipes();

    /**
     * Add a recipe to the map.
     * @param storage the recipe to add
     * @return the IToken.
     */
    IToken addRecipe(final IRecipeStorage storage);

    /**
     * Check if recipe is in map already, if not.
     * Add a recipe to the map.
     * @param storage the recipe to add
     * @return the iToken.
     */
    IToken checkOrAddRecipe(final IRecipeStorage storage);

    /**
     * Get the recipe id of a given recipeStorage.
     * @param storage the storage.
     * @return the id or null if inexistent.
     */
    IToken getRecipeId(final IRecipeStorage storage);

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void writeToNBT(@NotNull final CompoundNBT compound);


    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void readFromNBT(@NotNull final CompoundNBT compound);
}
