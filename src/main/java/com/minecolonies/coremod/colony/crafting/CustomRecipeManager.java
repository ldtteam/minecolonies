package com.minecolonies.coremod.colony.crafting;

import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.ResourceLocation;

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
    private HashMap<String, Map<ResourceLocation, CustomRecipe>> recipeMap = new HashMap<>();

    /**
     * The recipes that are marked for removal after loading all resource packs
     * This list will be processed on first access of the custom recipe list after load, and will be emptied.
     */
    private List<ResourceLocation> removedRecipes = new ArrayList<>();

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
    public void addRecipe(@NotNull final JsonObject recipeJson, @NotNull final ResourceLocation recipeLocation)
    {
        CustomRecipe recipe = CustomRecipe.parse(recipeJson);
        recipe.setRecipeId(recipeLocation);

        if(!recipeMap.containsKey(recipe.getCrafter()))
        {
            recipeMap.put(recipe.getCrafter(), new HashMap<>());
        }

        recipeMap.get(recipe.getCrafter()).put(recipeLocation, recipe);
    }

    /**
     * Remove a recipe from the manager
     * This allows modpacks to remove 'bad' recipes in addition to adding good ones
     * @param recipeJson
     * @param namespace
     * @param path
     */
    public void removeRecipe(@NotNull final JsonObject recipeJson, @NotNull final ResourceLocation recipeLocation)
    {
        if (recipeJson.has(RECIPE_TYPE_PROP) && recipeJson.get(RECIPE_TYPE_PROP).getAsString().equals(RECIPE_TYPE_REMOVE) && recipeJson.has(RECIPE_ID_TO_REMOVE_PROP))
        {
            ResourceLocation toRemove = new ResourceLocation(recipeJson.get(RECIPE_ID_TO_REMOVE_PROP).getAsString());
            if(!removedRecipes.contains(toRemove))
            {
                removedRecipes.add(toRemove);
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
        if(!removedRecipes.isEmpty())
        {
            for(ResourceLocation toRemove: removedRecipes)
            {
                final Optional<Map<ResourceLocation, CustomRecipe>> crafterMap = recipeMap.entrySet().stream().map(r -> r.getValue()).filter(r1 -> r1.keySet().contains(toRemove)).findFirst();
                if(crafterMap.isPresent())
                {
                    crafterMap.get().remove(toRemove);
                }
             }
            removedRecipes.clear();
        }

        if(recipeMap.containsKey(crafter))
        {
            return recipeMap.get(crafter).entrySet().stream().map(x -> x.getValue()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * The complete list of custom recipes, by crafter. 
     */
    public Map<String, Map<ResourceLocation, CustomRecipe>> getAllRecipes()
    {
        return recipeMap;
    }
    
}