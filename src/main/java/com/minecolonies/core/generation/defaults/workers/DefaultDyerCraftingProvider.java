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
 * Datagen for Dyer
 */
public class DefaultDyerCraftingProvider extends CustomRecipeProvider
{
    private static final String DYER = ModJobs.DYER_ID.getPath();

    public DefaultDyerCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultDyerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        new CustomRecipeBuilder(DYER, MODULE_CRAFTING, "red_sand")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.SAND, 4)),
                        new ItemStorage(new ItemStack(Items.RED_DYE))))
                .result(new ItemStack(Items.RED_SAND, 4))
                .build(consumer);

        new CustomRecipeBuilder(DYER, MODULE_CRAFTING, "black_dye")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.CHARCOAL))))
                .result(new ItemStack(Items.BLACK_DYE))
                .minBuildingLevel(2)
                .build(consumer);

        new CustomRecipeBuilder(DYER, MODULE_CRAFTING, "dark_prismarine")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.PRISMARINE, 4)),
                        new ItemStorage(new ItemStack(Items.BLACK_DYE))))
                .result(new ItemStack(Items.DARK_PRISMARINE, 4))
                .minBuildingLevel(3)
                .build(consumer);
    }
}
