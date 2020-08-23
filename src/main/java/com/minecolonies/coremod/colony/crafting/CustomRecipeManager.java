package com.minecolonies.coremod.colony.crafting;

import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.ldtteam.blockout.Log;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Manager class for tracking Custom recipes during load and use
 * This class is a singleton
 */
public class CustomRecipeManager
{
    private static CustomRecipeManager instance = new CustomRecipeManager();
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
    public void addRecipe(JsonObject recipeJson, String namespace, String path)
    {
        CustomRecipe recipe = CustomRecipe.parse(recipeJson);
        recipe.setRecipeId(namespace + ":" + path);
        Log.getLogger().info("Parsed: " + recipe.getRecipeId());

        if(!recipeMap.containsKey(recipe.getCrafter()))
        {
            Log.getLogger().info("Adding collection for: " + recipe.getCrafter());
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
    public void removeRecipe(JsonObject recipeJson, String namespace, String path)
    {
        final String id = namespace + ":" + path;
        if (recipeJson.has(RECIPE_TYPE) && recipeJson.get(RECIPE_TYPE).getAsString().equals("remove") && recipeJson.has("recipe-id-to-remove"))
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
    public Set<CustomRecipe> getRecipes(String crafter)
    {
        if(recipeMap.containsKey(crafter))
        {
            return recipeMap.get(crafter).entrySet().stream().map(x -> x.getValue()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
    
}