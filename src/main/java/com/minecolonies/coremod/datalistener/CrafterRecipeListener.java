package com.minecolonies.coremod.datalistener;

import com.google.gson.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;

import com.minecolonies.coremod.colony.crafting.SifterRecipe;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;
import static com.minecolonies.coremod.colony.crafting.SifterRecipe.RECIPE_TYPE_SIFTER;

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
    protected void apply(final Map<ResourceLocation, JsonElement> object, final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        Log.getLogger().info("Beginning load of custom recipes for colony workers");

        final CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final JsonObject recipeJson = entry.getValue().getAsJsonObject();

                final String recipeType = JSONUtils.getString(recipeJson, RECIPE_TYPE_PROP, "");

                if (recipeType.equals(RECIPE_TYPE_RECIPE))
                {
                    recipeManager.addRecipe(CustomRecipe.parse(key, recipeJson));
                }

                if (recipeType.equals(RECIPE_TYPE_RECIPE_MULT_OUT))
                {
                    recipeManager.addRecipe(CustomRecipe.parse(key, recipeJson));
                }

                if (recipeType.equals(RECIPE_TYPE_SIFTER))
                {
                    recipeManager.addRecipe(SifterRecipe.parse(key, recipeJson));
                }

                if (recipeType.equals(RECIPE_TYPE_REMOVE))
                {
                    final ResourceLocation toRemove = new ResourceLocation(JSONUtils.getString(recipeJson, RECIPE_ID_TO_REMOVE_PROP, ""));
                    recipeManager.removeRecipe(toRemove);
                }
            }
            catch (final JsonParseException e)
            {
                Log.getLogger().error("Error parsing crafterrecipe " + key.toString(), e);
            }
        } 

        final int totalRecipes = recipeManager.getAllRecipes().values().stream().mapToInt(x -> x.size()).sum();
        Log.getLogger().info("Loaded " + totalRecipes + " recipes for " + recipeManager.getAllRecipes().size() + " crafters");
        Log.getLogger().info("Loaded " + recipeManager.getSifterRecipes().size() + " sifter recipes");
    }
}