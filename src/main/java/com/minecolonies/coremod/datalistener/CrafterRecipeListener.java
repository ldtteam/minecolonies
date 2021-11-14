package com.minecolonies.coremod.datalistener;

import com.google.gson.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
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
     * Directory is: (namespace)/crafterrecipes/(path)
     * @param dataPackRegistries
     */
    public CrafterRecipeListener(@NotNull final DataPackRegistries dataPackRegistries)
    {
        super(GSON, "crafterrecipes");

        this.dataPackRegistries = dataPackRegistries;
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> object,
                         @NotNull final IResourceManager resourceManagerIn,
                         @NotNull final IProfiler profilerIn)
    {
        Log.getLogger().info("Beginning load of custom recipes for colony workers");

        final CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();
        recipeManager.reset();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final JsonObject recipeJson = entry.getValue().getAsJsonObject();

                switch (JSONUtils.getAsString(recipeJson, RECIPE_TYPE_PROP, ""))
                {
                    case RECIPE_TYPE_RECIPE:
                    case RECIPE_TYPE_RECIPE_MULT_OUT:
                        recipeManager.addRecipe(CustomRecipe.parse(key, recipeJson));
                        break;
                    case RECIPE_TYPE_REMOVE:
                        final ResourceLocation toRemove = new ResourceLocation(JSONUtils.getAsString(recipeJson, RECIPE_ID_TO_REMOVE_PROP, ""));
                        recipeManager.removeRecipe(toRemove);
                        break;
                }
            }
            catch (final JsonParseException e)
            {
                Log.getLogger().error("Error parsing crafterrecipe " + key.toString(), e);
            }
        }

        final int totalRecipes = recipeManager.getAllRecipes().values().stream().mapToInt(Map::size).sum();
        Log.getLogger().info("Loaded " + totalRecipes + " recipes for " + recipeManager.getAllRecipes().size() + " crafters");
    }
}