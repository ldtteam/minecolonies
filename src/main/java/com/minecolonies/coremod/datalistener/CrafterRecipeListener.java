package com.minecolonies.coremod.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Loader for Json based crafter specific recipes
 */
public class CrafterRecipeListener extends JsonReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataPackRegistries dataPackRegistries;

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: <namespace>/crafterrecipes/<path>
     * @param dataPackRegistries
     */
    public CrafterRecipeListener(@NotNull final DataPackRegistries dataPackRegistries)
    {
        super(GSON, "crafterrecipes");

        this.dataPackRegistries = dataPackRegistries;
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> object, final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        Log.getLogger().info("Beginning load of custom recipes for colony workers");

        CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();
        for(Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            ResourceLocation key = entry.getKey();

            JsonObject recipeJson = entry.getValue().getAsJsonObject();

            if(recipeJson.has(RECIPE_TYPE_PROP) && recipeJson.get(RECIPE_TYPE_PROP).getAsString().equals(RECIPE_TYPE_RECIPE))
            {
                recipeManager.addRecipe(recipeJson, key);
            }

            if(recipeJson.has(RECIPE_TYPE_PROP) && recipeJson.get(RECIPE_TYPE_PROP).getAsString().equals(RECIPE_TYPE_RECIPE_MULT_OUT)) 
            {
                recipeManager.addRecipe(recipeJson, key);
            }

            if(recipeJson.has(RECIPE_TYPE_PROP) && recipeJson.get(RECIPE_TYPE_PROP).getAsString().equals(RECIPE_TYPE_REMOVE)) 
            {
                recipeManager.removeRecipe(recipeJson, key);
            }
        } 

        final int totalRecipes = recipeManager.getAllRecipes().values().stream().mapToInt(x -> x.size()).sum();
        Log.getLogger().info("Loaded " + totalRecipes + " recipes for " + recipeManager.getAllRecipes().size() + " crafters");

        IColonyManager.getInstance().getCompatibilityManager().invalidateRecipes(this.dataPackRegistries.getRecipeManager());
    }
}