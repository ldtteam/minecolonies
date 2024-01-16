package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Cook Assistant
 */
public class DefaultCookAssistantCraftingProvider extends CustomRecipeProvider
{
    private static final String COOKASSISTANT = ModJobs.COOKASSISTANT_ID.getPath();

    public DefaultCookAssistantCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultCookAssistantCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
    }
}
