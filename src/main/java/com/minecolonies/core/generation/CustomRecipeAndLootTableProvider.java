package com.minecolonies.core.generation;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Abstract datagen for crafterrecipes that include loot tables.
 */
public abstract class CustomRecipeAndLootTableProvider implements DataProvider
{
    private final ChildRecipeProvider recipeProvider;
    private final ChildLootTableProvider lootTableProvider;

    protected CustomRecipeAndLootTableProvider(@NotNull final PackOutput packOutput)
    {
        recipeProvider = new ChildRecipeProvider(packOutput);
        lootTableProvider = new ChildLootTableProvider(packOutput);
    }

    protected abstract void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer);
    protected abstract void registerTables(@NotNull final SimpleLootTableProvider.LootTableRegistrar registrar);

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        return CompletableFuture.allOf(recipeProvider.run(cache), lootTableProvider.run(cache));
    }

    private class ChildRecipeProvider extends CustomRecipeProvider
    {
        public ChildRecipeProvider(@NotNull final PackOutput packOutput)
        {
            super(packOutput);
        }

        @NotNull
        @Override
        public String getName()
        {
            return CustomRecipeAndLootTableProvider.this.getName() + " recipes";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
        {
            CustomRecipeAndLootTableProvider.this.registerRecipes(consumer);
        }
    }

    private class ChildLootTableProvider extends SimpleLootTableProvider
    {
        public ChildLootTableProvider(@NotNull final PackOutput packOutput)
        {
            super(packOutput);
        }

        @NotNull
        @Override
        public String getName()
        {
            return CustomRecipeAndLootTableProvider.this.getName() + " loot tables";
        }

        @Override
        protected void registerTables(@NotNull final LootTableRegistrar registrar)
        {
            CustomRecipeAndLootTableProvider.this.registerTables(registrar);
        }
    }
}
