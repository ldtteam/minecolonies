package com.minecolonies.core.generation;

import com.minecolonies.core.generation.CustomRecipeProvider.CustomRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Abstract datagen for crafterrecipes that include loot tables.
 */
public abstract class CustomRecipeAndLootTableProvider implements DataProvider
{
    private final CompletableFuture<HolderLookup.Provider> providerFuture;
    private final ChildRecipeProvider recipeProvider;
    private final ChildLootTableProvider lootTableProvider;
    protected HolderLookup.Provider provider;

    protected CustomRecipeAndLootTableProvider(@NotNull final PackOutput packOutput,
                                               @NotNull final CompletableFuture<HolderLookup.Provider> providerFuture)
    {
        this.providerFuture = providerFuture;
        this.recipeProvider = new ChildRecipeProvider(packOutput, providerFuture);
        this.lootTableProvider = new ChildLootTableProvider(packOutput, providerFuture);
    }

    /**
     * Override this if you need to do some additional async work prior to calling the registration functions.
     * @param provider the registry provider
     * @return a future
     */
    protected CompletableFuture<?> generate(@NotNull final HolderLookup.Provider provider)
    {
        return CompletableFuture.completedFuture(null);
    }

    protected abstract void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer);
    protected abstract @NotNull List<LootTableProvider.SubProviderEntry> registerTables();

    protected CustomRecipeBuilder recipe(final String crafter, final String module, final String id)
    {
        return recipeProvider.recipe(crafter, module, id);
    }

    protected static ResourceKey<LootTable> table(@NotNull final ResourceLocation id)
    {
        return SimpleLootTableProvider.table(id);
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        return providerFuture
                .thenComposeAsync(provider -> {
                    this.provider = provider;
                    return generate(provider);
                })
                .thenCompose(x -> CompletableFuture.allOf(recipeProvider.run(cache), lootTableProvider.run(cache)));
    }

    private class ChildRecipeProvider extends CustomRecipeProvider
    {
        public ChildRecipeProvider(@NotNull final PackOutput packOutput,
                                   @NotNull final CompletableFuture<HolderLookup.Provider> provider)
        {
            super(packOutput, provider);
        }

        @NotNull
        @Override
        public String getName()
        {
            return CustomRecipeAndLootTableProvider.this.getName() + " recipes";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
        {
            CustomRecipeAndLootTableProvider.this.registerRecipes(consumer);
        }
    }

    private class ChildLootTableProvider extends SimpleLootTableProvider
    {
        public ChildLootTableProvider(@NotNull final PackOutput packOutput,
                                      @NotNull final CompletableFuture<HolderLookup.Provider> provider)
        {
            super(packOutput, provider);
        }

        @NotNull
        @Override
        public List<SubProviderEntry> getTables()
        {
            return CustomRecipeAndLootTableProvider.this.registerTables();
        }
    }
}
