package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * The Interface describing the recipeManager which takes care of the recipes discovered by the colonies in this world.
 */
public interface IRecipeManager
{
    /**
     * Get a unmodifiable copy of the recipes map.
     *
     * @return a map of Token, RecipeStorage.
     */
    ImmutableMap<IToken<?>, IRecipeStorage> getRecipes();

    /**
     * Get a recipe from the storage.
     *
     * @param token the unique token.
     * @return the recipe.
     */
    IRecipeStorage getRecipe(final IToken<?> token);

    /**
     * Add a recipe to the map.
     *
     * @param storage the recipe to add
     * @return the IToken.
     */
    IToken<?> addRecipe(final IRecipeStorage storage);

    /**
     * Check if recipe is in map already, if not. Add a recipe to the map.
     *
     * @param storage the recipe to add
     * @return the iToken.
     */
    IToken<?> checkOrAddRecipe(final IRecipeStorage storage);

    /**
     * Get the recipe id of a given recipeStorage.
     *
     * @param storage the storage.
     * @return the id or null if inexistent.
     */
    IToken<?> getRecipeId(final IRecipeStorage storage);

    /**
     * Register the recipe as used with the recipe manager
     * 
     * @param token the recipe token
     */
    void registerUse(final IToken<?> token);

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void write(@NotNull final CompoundTag compound);

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void read(@NotNull final CompoundTag compound);

    /**
     * Clear the recipe list (used during shutdown)
     */
    void reset();
}
