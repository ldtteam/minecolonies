package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Fletcher
 */
public class DefaultFletcherCraftingProvider extends CustomRecipeProvider
{
    private static final String FLETCHER = ModJobs.FLETCHER_ID.getPath();

    public DefaultFletcherCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultFletcherCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        new CustomRecipeBuilder(FLETCHER, MODULE_CRAFTING, "flint")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.GRAVEL, 3))))
                .result(new ItemStack(Items.FLINT))
                .build(consumer);

        new CustomRecipeBuilder(FLETCHER, MODULE_CRAFTING, "string")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHITE_WOOL))))
                .result(new ItemStack(Items.STRING, 4))
                .build(consumer);
    }
}
