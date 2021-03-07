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

    private Map<ResourceLocation, SifterRecipe> sifterRecipeMap = new HashMap<>();

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
     * Add recipe to manager.
     * @param recipe the recipe to add
     */
    public void addRecipe(@NotNull final CustomRecipe recipe)
    {
        if(!recipeMap.containsKey(recipe.getCrafter()))
        {
            recipeMap.put(recipe.getCrafter(), new HashMap<>());
        }

        recipeMap.get(recipe.getCrafter()).put(recipe.getRecipeId(), recipe);
    }

    public void addRecipe(@NotNull final SifterRecipe recipe)
    {
        sifterRecipeMap.put(recipe.getRecipeId(), recipe);
    }

    /**
     * Remove recipe
     * @param toRemove
     */
    public void removeRecipe(@NotNull final ResourceLocation toRemove)
    {
        if(!removedRecipes.contains(toRemove))
        {
            removedRecipes.add(toRemove);
        }
    }

    /**
     * Get all of the custom recipes that apply to a particular crafter (other than special cases)
     * @param crafter
     * @return
     */
    public Set<CustomRecipe> getRecipes(@NotNull final String crafter)
    {
        removeRecipes();

        return Collections.unmodifiableSet(new HashSet<>(recipeMap.getOrDefault(crafter, new HashMap<>()).values()));
    }

    /**
     * Get all of the custom recipes that apply to the sifter
     * @return
     */
    public Set<SifterRecipe> getSifterRecipes()
    {
        removeRecipes();

        return Collections.unmodifiableSet(new HashSet<>(sifterRecipeMap.values()));
    }

    /**
     * The complete list of custom recipes, by crafter. 
     */
    public Map<String, Map<ResourceLocation, CustomRecipe>> getAllRecipes()
    {
        return recipeMap;
    }

    private void removeRecipes()
    {
        if (!removedRecipes.isEmpty())
        {
            for (final ResourceLocation toRemove : removedRecipes)
            {
                recipeMap.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .filter(r1 -> r1.containsKey(toRemove))
                    .findFirst()
                    .ifPresent(crafterRecipeMap -> crafterRecipeMap.remove(toRemove));

                sifterRecipeMap.remove(toRemove);
            }

            removedRecipes.clear();
        }
    }
}