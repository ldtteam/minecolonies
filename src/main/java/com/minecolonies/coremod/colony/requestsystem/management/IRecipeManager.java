package com.minecolonies.coremod.colony.requestsystem.management;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public interface IRecipeManager
{
    /**
     * Get a unmodifiable copy of the recipes map.
     * @return a map of Token, RecipeStorage.
     */
    ImmutableMap<Object, Object> getRecipes();

    /**
     * Add a recipe to the map.
     * @param storage the recipe to add
     */
    IToken addRecipe(final RecipeStorage storage);

    /**
     * Check if recipe is in map already, if not.
     * Add a recipe to the map.
     * @param storage the recipe to add
     */
    IToken checkOrAddRecipe(final RecipeStorage storage);

    /**
     * Get the recipe id of a given recipeStorage.
     * @param storage the storage.
     * @return the id or null if inexistent.
     */
    IToken getRecipeId(final RecipeStorage storage);

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void writeToNBT(@NotNull final NBTTagCompound compound);


    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void readFromNBT(@NotNull final NBTTagCompound compound);
}
