package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Stonemason
 */
public class DefaultStonemasonCraftingProvider extends CustomRecipeProvider
{
    private static final String STONEMASON = ModJobs.STONEMASON_ID.getPath();

    public DefaultStonemasonCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultStonemasonCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        convert(consumer, Items.COBBLESTONE, Items.SAND, Items.SANDSTONE);
        convert(consumer, Items.COBBLESTONE, Items.RED_SAND, Items.RED_SANDSTONE);
        convert(consumer, Items.COBBLESTONE, Items.PRISMARINE_SHARD, Items.PRISMARINE);
        convert(consumer, Items.STONE_BRICKS, Items.PRISMARINE_SHARD, Items.PRISMARINE_BRICKS);

        recipe(STONEMASON, MODULE_CRAFTING, "end_stone")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.SANDSTONE, 8)),
                        new ItemStorage(new ItemStack(Items.ENDER_PEARL))))
                .result(new ItemStack(Items.END_STONE, 8))
                .minResearchId(ResearchConstants.THE_END)
                .build(consumer);
    }

    private void convert(@NotNull final Consumer<CustomRecipeBuilder> consumer,
                         @NotNull final ItemLike input1,
                         @NotNull final ItemLike input2,
                         @NotNull final ItemLike output)
    {
        recipe(STONEMASON, MODULE_CRAFTING,
                        BuiltInRegistries.ITEM.getKey(output.asItem()).getPath())
                .inputs(List.of(new ItemStorage(new ItemStack(input1)), new ItemStorage(new ItemStack(input2))))
                .result(new ItemStack(output))
                .build(consumer);
    }
}
