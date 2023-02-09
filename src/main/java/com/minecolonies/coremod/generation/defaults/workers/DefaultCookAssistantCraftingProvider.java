package com.minecolonies.coremod.generation.defaults.workers;

import com.minecolonies.coremod.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Datagen for Cook Assistant
 */
public class DefaultCookAssistantCraftingProvider extends CustomRecipeProvider
{
    public DefaultCookAssistantCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
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
