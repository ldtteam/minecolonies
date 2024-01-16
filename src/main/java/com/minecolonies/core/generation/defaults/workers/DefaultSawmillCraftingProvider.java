package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Sawmill
 */
public class DefaultSawmillCraftingProvider extends CustomRecipeProvider
{
    private static final String SAWMILL = ModJobs.SAWMILL_ID.getPath();

    public DefaultSawmillCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultSawmillCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
    }
}
