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
          .inputs(List.of(new ItemStorage(ModItems.large_milk_bottle.toStack())))
          .result(ModItems.butter.toStack())
          .secondaryOutputs(List.of(ModItems.large_empty_bottle.toStack()))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cabochis")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(ModItems.manchet_bread.toStack())))
          .result(ModItems.cabochis.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cheddar_cheese")
          .inputs(List.of(new ItemStorage(ModItems.large_milk_bottle.toStack())))
          .result(ModItems.cheddar_cheese.toStack())
          .secondaryOutputs(List.of(ModItems.large_empty_bottle.toStack()))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "congee")
          .inputs(List.of(
            new ItemStorage(ModItems.cooked_rice.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage))))
          .result(ModItems.congee.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "cooked_rice")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockRice)),
            new ItemStorage(new ItemStack(Items.BOWL))))
          .result(ModItems.cooked_rice.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "eggplant_dolma")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(ModItems.feta_cheese.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(ModItems.eggplant_dolma.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "feta_cheese")
          .inputs(List.of(new ItemStorage(ModItems.large_milk_bottle.toStack())))
          .result(ModItems.feta_cheese.toStack())
          .secondaryOutputs(List.of(ModItems.large_empty_bottle.toStack()))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(ModItems.hand_pie.toStack())
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
          .result(ModItems.lamb_stew.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pasta_plain")
          .inputs(List.of(
            new ItemStorage(ModItems.raw_noodle.toStack()),
            new ItemStorage(ModItems.butter.toStack()),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(ModItems.pasta_plain.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pasta_tomato")
          .inputs(List.of(
            new ItemStorage(ModItems.raw_noodle.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(ModItems.pasta_tomato.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pita_hummus")
          .inputs(List.of(
            new ItemStorage(ModItems.flatbread.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(ModItems.pita_hummus.toStack())
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
          .result(ModItems.pottage.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "raw_noodle")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum))))
          .result(ModItems.raw_noodle.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "rice_ball")
          .inputs(List.of(
            new ItemStorage(ModItems.tofu.toStack()),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(ModItems.cooked_rice.toStack())))
          .result(ModItems.rice_ball.toStack(2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stew_trencher")
          .inputs(List.of(
            new ItemStorage(ModItems.manchet_bread.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(ModItems.stew_trencher.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pepper")
          .inputs(List.of(
            new ItemStorage(ModItems.cooked_rice.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant))))
          .result(ModItems.stuffed_pepper.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(ModItems.flatbread.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(ModItems.stuffed_pita.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(ModItems.cooked_rice.toStack()),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(ModItems.sushi_roll.toStack(2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "tofu")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)), new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(ModItems.tofu.toStack())
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pepper_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea))))
          .result(ModItems.pepper_hummus.toStack(2))
          .minBuildingLevel(1)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "soy_milk")
          .inputs(List.of(
            new ItemStorage(ModItems.large_water_bottle.toStack()),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(ModItems.large_soy_milk_bottle.toStack())
          .minBuildingLevel(1)
          .build(consumer);
    }
}
