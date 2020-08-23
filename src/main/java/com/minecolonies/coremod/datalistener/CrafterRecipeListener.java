package com.minecolonies.coremod.datalistener;

import com.google.gson.*;
import com.ldtteam.blockout.Log;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Loader for Json based crafter specific recipes
 */
public class CrafterRecipeListener extends JsonReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: <namespace>/crafterrecipes/<path>
     */
    public CrafterRecipeListener()
    {
        super(GSON, "crafterrecipes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> object, IResourceManager resourceManager, IProfiler profiler)
    {
        Log.getLogger().info("Beginning load of custom recipes for colony workers");

        CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();
        for(Map.Entry<ResourceLocation, JsonObject> entry : object.entrySet())
        {
            ResourceLocation key = entry.getKey();
            JsonObject recipeJson = entry.getValue();

            if(recipeJson.has(RECIPE_TYPE) && recipeJson.get(RECIPE_TYPE).getAsString().equals(RECIPE_TYPE_RECIPE)) 
            {
                recipeManager.addRecipe(recipeJson, key.getNamespace(), key.getPath());
            }

            if(recipeJson.has(RECIPE_TYPE) && recipeJson.get(RECIPE_TYPE).getAsString().equals(RECIPE_TYPE_REMOVE)) 
            {
                recipeManager.removeRecipe(recipeJson, key.getNamespace(), key.getPath());
            }
        } 
    }
}