package com.minecolonies.core.datalistener;

import com.google.gson.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.minecolonies.core.colony.crafting.CustomRecipe.*;

/**
 * Loader for Json based crafter specific recipes
 */
public class CrafterRecipeListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: (namespace)/crafterrecipes/(path)
     */
    public CrafterRecipeListener()
    {
        super(GSON, "crafterrecipes");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> object,
                         @NotNull final ResourceManager resourceManagerIn,
                         @NotNull final ProfilerFiller profilerIn)
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

                switch (GsonHelper.getAsString(recipeJson, RECIPE_TYPE_PROP, ""))
                {
                    case RECIPE_TYPE_RECIPE:
                    case RECIPE_TYPE_RECIPE_MULT_OUT:
                        recipeManager.addRecipe(CustomRecipe.parse(key, recipeJson));
                        break;
                    case RECIPE_TYPE_TEMPLATE:
                        recipeManager.addRecipeTemplate(key, recipeJson);
                        break;
                    case RECIPE_TYPE_REMOVE:
                        final ResourceLocation toRemove = new ResourceLocation(GsonHelper.getAsString(recipeJson, RECIPE_ID_TO_REMOVE_PROP, ""));
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