package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
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
 * Datagen for Chef.
 */
public class DefaultChefCraftingProvider extends CustomRecipeProvider
{
    private static final String CHEF = ModJobs.CHEF_ID.getPath();

    public DefaultChefCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultChefCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        recipe(CHEF, MODULE_CRAFTING, "butter")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.butter))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cabochis")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModItems.manchet_bread))))
          .result(new ItemStack(ModItems.cabochis))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cheddar_cheese")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.cheddar_cheese))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "congee")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage))))
          .result(new ItemStack(ModItems.congee))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cooked_rice")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockRice)),
            new ItemStorage(new ItemStack(Items.BOWL))))
          .result(new ItemStack(ModItems.cooked_rice))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "eggplant_dolma")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModItems.feta_cheese)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.eggplant_dolma))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "feta_cheese")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.feta_cheese))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(new ItemStack(ModItems.hand_pie))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "lamb_stew")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(new ItemStack(ModItems.lamb_stew))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pasta_plain")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.raw_noodle)),
            new ItemStorage(new ItemStack(ModItems.butter)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pasta_plain))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pasta_tomato")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.raw_noodle)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pasta_tomato))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pita_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pita_hummus))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pottage")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pottage))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "raw_noodle")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum))))
          .result(new ItemStack(ModItems.raw_noodle))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "rice_ball")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.tofu)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModItems.cooked_rice))))
          .result(new ItemStack(ModItems.rice_ball, 2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stew_trencher")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.manchet_bread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.stew_trencher))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pepper")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant))))
          .result(new ItemStack(ModItems.stuffed_pepper))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.stuffed_pita))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.sushi_roll, 2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "tofu")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)), new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(new ItemStack(ModItems.tofu))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pepper_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea))))
          .result(new ItemStack(ModItems.pepper_hummus, 2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "soy_milk")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.large_water_bottle)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(new ItemStack(ModItems.large_soy_milk_bottle))
          .minBuildingLevel(1)
          .build(consumer);
    }
}
