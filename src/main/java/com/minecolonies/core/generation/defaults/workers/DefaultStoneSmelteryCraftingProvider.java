package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for StoneSmeltery
 */
public class DefaultStoneSmelteryCraftingProvider extends CustomRecipeProvider
{
    private static final String STONE_SMELTERY = ModJobs.STONE_SMELTERY_ID.getPath();

    public DefaultStoneSmelteryCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultStoneSmelteryCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
    }
}
