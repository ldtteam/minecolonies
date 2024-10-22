package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Chef.
 */
public class DefaultChefCraftingProvider extends CustomRecipeProvider
{
    private static final String CHEF = ModJobs.CHEF_ID.getPath();

    public DefaultChefCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultChefCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "butter")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.butter))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "cabochis")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModItems.manchet_bread))))
          .result(new ItemStack(ModItems.cabochis))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "cheddar_cheese")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.cheddar_cheese))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "congee")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage))))
          .result(new ItemStack(ModItems.congee))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "cooked_rice")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockRice)),
            new ItemStorage(new ItemStack(Items.BOWL))))
          .result(new ItemStack(ModItems.cooked_rice))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "eggplant_dolma")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "feta_cheese")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.feta_cheese))
          .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(new ItemStack(ModItems.hand_pie))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "lamb_stew")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pasta_plain")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.raw_noodle)),
            new ItemStorage(new ItemStack(ModItems.butter)),
            new ItemStorage(new ItemStack(Items.BOWL)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pasta_plain))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pasta_tomato")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pita_hummus")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pottage")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "raw_noodle")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum))))
          .result(new ItemStack(ModItems.raw_noodle))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "rice_ball")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.tofu)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModItems.cooked_rice))))
          .result(new ItemStack(ModItems.rice_ball, 2))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stew_trencher")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.manchet_bread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.stew_trencher))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stuffed_pepper")
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

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.stuffed_pita))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.sushi_roll, 2))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "tofu")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)), new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(new ItemStack(ModItems.tofu))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pepper_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea))))
          .result(new ItemStack(ModItems.pepper_hummus, 2))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "soy_milk")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.large_water_bottle)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean)),
            new ItemStorage(new ItemStack(ModBlocks.blockSoyBean))))
          .result(new ItemStack(ModItems.large_soy_milk_bottle))
          .minBuildingLevel(1)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "mint_jelly")
                .inputs(List.of(
                    new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                    new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                    new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                    new ItemStorage(ModItems.large_water_bottle.getDefaultInstance()),
                    new ItemStorage(new ItemStack(Items.SUGAR))
			    ))
		.result(new ItemStack(ModItems.mint_jelly, 2))
            .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
            .minBuildingLevel(1)
            .showTooltip(true)
            .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "cheese_ravioli")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModItems.feta_cheese))
                        ))
                .result(new ItemStack(ModItems.cheese_ravioli, 4))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "chicken_broth")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.CHICKEN)),
                        new ItemStorage(ModItems.large_water_bottle.getDefaultInstance()),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.chicken_broth, 2))
                .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "corn_chowder")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModBlocks.blockCorn)),
                        new ItemStorage(new ItemStack(Items.MILK_BUCKET)),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.corn_chowder, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "grilled_chicken")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(Items.CHICKEN)),
                        new ItemStorage(new ItemStack(ModItems.butter))
                        ))
                .result(new ItemStack(ModItems.grilled_chicken, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "meat_ravioli")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(Items.BEEF))
                        ))
                .result(new ItemStack(ModItems.meat_ravioli, 4))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "mint_jelly")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                        new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                        new ItemStorage(new ItemStack(ModBlocks.blockMint)),
                        new ItemStorage(ModItems.large_water_bottle.getDefaultInstance()),
                        new ItemStorage(new ItemStack(Items.SUGAR))
                        ))
                .result(new ItemStack(ModItems.mint_jelly, 2))
                .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "mutton_kebab")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(Items.STICK)),
                        new ItemStorage(new ItemStack(Items.MUTTON)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic))
                        ))
                .result(new ItemStack(ModItems.kebab, 4))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pea_soup")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModBlocks.blockPeas)),
                        new ItemStorage(new ItemStack(Items.MILK_BUCKET)),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.pea_soup, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "polenta")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModItems.cornmeal)),
                        new ItemStorage(new ItemStack(ModItems.cornmeal)),
                        new ItemStorage(new ItemStack(ModBlocks.blockButternutSquash)),
                        new ItemStorage(ModItems.large_water_bottle.getDefaultInstance()),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.polenta, 2))
                .secondaryOutputs(List.of(new ItemStack(ModItems.large_empty_bottle)))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "potato_soup")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(Items.MILK_BUCKET)),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.potato_soup, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "squash_soup")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModBlocks.blockButternutSquash)),
                        new ItemStorage(new ItemStack(Items.MILK_BUCKET)),
                        new ItemStorage(new ItemStack(Items.BOWL))
                        ))
                .result(new ItemStack(ModItems.squash_soup, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "tortillas")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModItems.cornmeal)),
                        new ItemStorage(new ItemStack(ModItems.cornmeal))
                        ))
                .result(new ItemStack(ModItems.tortillas, 4))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "veggie_ravioli")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(Items.BEETROOT))
                        ))
                .result(new ItemStack(ModItems.veggie_ravioli, 4))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "yogurt")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(Items.MILK_BUCKET))
                ))
                .result(new ItemStack(ModItems.yogurt, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "baked_salmon")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(Items.SALMON)),
                        new ItemStorage(new ItemStack(Items.SALMON))
                ))
                .result(new ItemStack(ModItems.baked_salmon, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "eggdrop_soup")
                .inputs(List.of(
                    new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                    new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                    new ItemStorage(new ItemStack(Items.EGG)),
                    new ItemStorage(new ItemStack(Items.EGG)),
                    new ItemStorage(new ItemStack(Items.CHICKEN))
                ))
                .result(new ItemStack(ModItems.eggdrop_soup, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "fish_n_chips")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.SALMON)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum))
                        ))
                .result(new ItemStack(ModItems.fish_n_chips, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "kimchi")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
                        new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
                        new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
                        new ItemStorage(new ItemStack(Items.SALMON)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.CARROT))
                        ))
                .result(new ItemStack(ModItems.kimchi, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pierogi")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModItems.cheddar_cheese))
                        ))
                .result(new ItemStack(ModItems.pierogi, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "veggie_quiche")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.EGG)),
                        new ItemStorage(new ItemStack(Items.EGG)),
                        new ItemStorage(new ItemStack(Items.EGG))
                        ))
                .result(new ItemStack(ModItems.veggie_quiche, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "veggie_soup")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
                        new ItemStorage(new ItemStack(Items.CARROT))
                        ))
                .result(new ItemStack(ModItems.veggie_soup, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "yogurt_with_berries")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModItems.yogurt)),
                        new ItemStorage(new ItemStack(Items.SWEET_BERRIES)),
                        new ItemStorage(new ItemStack(Items.SWEET_BERRIES)),
                        new ItemStorage(new ItemStack(Items.SWEET_BERRIES)),
                        new ItemStorage(new ItemStack(Items.SWEET_BERRIES))
                        ))
                .result(new ItemStack(ModItems.yogurt_with_berries, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "borscht")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModItems.chicken_broth)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(Items.BEETROOT)),
                        new ItemStorage(new ItemStack(Items.BEETROOT))
                        ))
                .result(new ItemStack(ModItems.borscht, 2))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "fish_dinner")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
                        new ItemStorage(new ItemStack(Items.COD)),
                        new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
                        ))
                .result(new ItemStack(ModItems.fish_dinner, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "mutton_dinner")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModItems.mint_jelly)),
                        new ItemStorage(new ItemStack(Items.MUTTON)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(ModBlocks.blockPeas))
                        ))
                .result(new ItemStack(ModItems.mutton_dinner, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "ramen")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(ModItems.raw_noodle)),
                        new ItemStorage(new ItemStack(ModItems.soysauce))
                        ))
                .result(new ItemStack(ModItems.ramen, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "schnitzel")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModItems.manchet_bread)),
                        new ItemStorage(new ItemStack(Items.PORKCHOP)),
                        new ItemStorage(new ItemStack(Items.EGG)),
                        new ItemStorage(new ItemStack(Items.POTATO))
                        ))
                .result(new ItemStack(ModItems.schnitzel, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "steak_dinner")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
                        new ItemStorage(new ItemStack(Items.BEEF)),
                        new ItemStorage(new ItemStack(Items.POTATO)),
                        new ItemStorage(new ItemStack(Items.POTATO))
                        ))
                .result(new ItemStack(ModItems.steak_dinner, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "tacos")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
                        new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
                        new ItemStorage(new ItemStack(ModItems.tortillas)),
                        new ItemStorage(new ItemStack(Items.BEEF)),
                        new ItemStorage(new ItemStack(ModBlocks.blockTomato))
                        ))
                .result(new ItemStack(ModItems.tacos, 1))
                .minBuildingLevel(1)
                .showTooltip(true)
                .build(consumer);
    }
}
