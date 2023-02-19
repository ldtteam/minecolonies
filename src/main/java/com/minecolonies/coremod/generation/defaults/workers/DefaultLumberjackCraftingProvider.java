package com.minecolonies.coremod.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;

/**
 * Datagen for Lumberjack
 */
public class DefaultLumberjackCraftingProvider extends CustomRecipeProvider
{
    private static final String LUMBERJACK = ModJobs.LUMBERJACK_ID.getPath();

    public DefaultLumberjackCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultLumberjackCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        strip(consumer, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.ACACIA_WOOD, Items.STRIPPED_ACACIA_WOOD);
        strip(consumer, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.BIRCH_WOOD, Items.STRIPPED_BIRCH_WOOD);
        strip(consumer, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.DARK_OAK_WOOD, Items.STRIPPED_DARK_OAK_WOOD);
        strip(consumer, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.JUNGLE_WOOD, Items.STRIPPED_JUNGLE_WOOD);
        strip(consumer, Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.OAK_WOOD, Items.STRIPPED_OAK_WOOD);
        strip(consumer, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.SPRUCE_WOOD, Items.STRIPPED_SPRUCE_WOOD);
        
        strip(consumer, Items.CRIMSON_STEM, Items.STRIPPED_CRIMSON_STEM, Items.CRIMSON_HYPHAE, Items.STRIPPED_CRIMSON_HYPHAE);
        strip(consumer, Items.WARPED_STEM, Items.STRIPPED_WARPED_STEM, Items.WARPED_HYPHAE, Items.STRIPPED_WARPED_HYPHAE);
    }
    
    private void strip(@NotNull final Consumer<FinishedRecipe> consumer,
                       @NotNull final Item inputLog,
                       @NotNull final Item strippedOutputLog,
                       @NotNull final Item... alternativeOutputs)
    {
        CustomRecipeBuilder.create(LUMBERJACK, MODULE_CUSTOM, "stripped_" + ForgeRegistries.ITEMS.getKey(inputLog).getPath())
                .inputs(List.of(new ItemStorage(new ItemStack(inputLog))))
                .result(new ItemStack(strippedOutputLog))
                .alternateOutputs(Arrays.stream(alternativeOutputs).map(ItemStack::new).toList())
                .requiredTool(ToolType.AXE)
                .build(consumer);
    }
}
