package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The Interface describing the recipeManager which takes care of the recipes discovered by the colonies in this world.
 */
public interface IRecipeManager
{
    /**
     * Get a unmodifiable copy of the recipes map.
     * @return a map of Token, RecipeStorage.
     */
    Map<IToken, RecipeStorage> getRecipes();

    /**
     * Add a recipe to the map.
     * @param storage the recipe to add
     * @return the IToken.
     */
    IToken addRecipe(final RecipeStorage storage);

    /**
     * Check if recipe is in map already, if not.
     * Add a recipe to the map.
     * @param storage the recipe to add
     * @return the iToken.
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
