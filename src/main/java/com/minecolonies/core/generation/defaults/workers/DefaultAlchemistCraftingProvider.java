package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Alchemist
 */
public class DefaultAlchemistCraftingProvider extends CustomRecipeProvider
{
    private final String ALCHEMIST = ModJobs.ALCHEMIST_ID.getPath();

    public DefaultAlchemistCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultAlchemistCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        new CustomRecipeBuilder(ALCHEMIST, MODULE_CRAFTING, "magicpotion")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.mistletoe)),
                        new ItemStorage(ModItems.large_water_bottle.getDefaultInstance())))
                .result(new ItemStack(ModItems.magicpotion))
                .minResearchId(ResearchConstants.DRUID_USE_POTIONS)
                .showTooltip(true)
                .build(consumer);
    }
}
