package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Glassblower
 */
public class DefaultGlassblowerCraftingProvider extends CustomRecipeProvider
{
    private static final String GLASSBLOWER = ModJobs.GLASSBLOWER_ID.getPath();

    public DefaultGlassblowerCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultGlassblowerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
    }
}
