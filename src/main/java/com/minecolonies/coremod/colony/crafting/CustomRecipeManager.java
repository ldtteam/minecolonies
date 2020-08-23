package com.minecolonies.coremod.colony.crafting;

import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.ldtteam.blockout.Log;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Manager class for tracking Custom recipes during load and use
 * This class is a singleton
 */
public class CustomRecipeManager
{
    /**
     * The internal static instance of the singleton
     */
    private static CustomRecipeManager instance = new CustomRecipeManager();

    /**
     * The map of loaded recipes
     */
    private HashMap<String, HashMap<String, CustomRecipe>> recipeMap = new HashMap<>();

    private CustomRecipeManager()
    {
    }
        
    /**
     * Get the singleton instance of this class
     * @return
     */
    public static CustomRecipeManager getInstance()
    {
        return instance;
    }

    /**
     * Add a recipe Json to the manager
     * @param recipeJson
     * @param namespace
     * @param path
     */
    public void addRecipe(@NotNull final JsonObject recipeJson, @NotNull final String namespace, @NotNull final String path)
    {
        CustomRecipe recipe = CustomRecipe.parse(recipeJson);
        recipe.setRecipeId(namespace + ":" + path);

        if(!recipeMap.containsKey(recipe.getCrafter()))
        {
            recipeMap.put(recipe.getCrafter(), new HashMap<>());
        }
        recipeMap.get(recipe.getCrafter()).put(recipe.getRecipeId(), recipe);
    }

    /**
     * Remove a recipe from the manager
     * This allows modpacks to remove 'bad' recipes in addition to adding good ones
     * @param recipeJson
     * @param namespace
     * @param path
     */
    public void removeRecipe(@NotNull final JsonObject recipeJson, @NotNull final String namespace, @NotNull final String path)
    {
        final String id = namespace + ":" + path;
        if (recipeJson.has(RECIPE_TYPE_PROP) && recipeJson.get(RECIPE_TYPE_PROP).getAsString().equals("remove") && recipeJson.has("recipe-id-to-remove"))
        {
            final Optional<HashMap<String, CustomRecipe>> crafterMap = recipeMap.entrySet().stream().map(r -> r.getValue()).filter(r1 -> r1.keySet().contains(id)).findFirst();
            if(crafterMap.isPresent())
            {
                crafterMap.get().remove(id);
            }
        }
    }

    /**
     * Get all of the custom recipes that apply to a particular crafter
     * @param crafter
     * @return
     */
    public Set<CustomRecipe> getRecipes(@NotNull final String crafter)
    {
        if(recipeMap.containsKey(crafter))
        {
            return recipeMap.get(crafter).entrySet().stream().map(x -> x.getValue()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
    
}