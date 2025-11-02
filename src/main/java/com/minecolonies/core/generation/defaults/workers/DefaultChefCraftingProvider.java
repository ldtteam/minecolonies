package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModFoodItems;
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
        recipe(CHEF, MODULE_CRAFTING, "stew_trencher")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModFoodItems.manchet_bread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModFoodItems.stew_trencher))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pepper")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModFoodItems.cooked_rice)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant))))
          .result(new ItemStack(ModFoodItems.stuffed_pepper))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pita_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModFoodItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModFoodItems.pita_hummus))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "eggplant_dolma")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModFoodItems.feta_cheese)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModFoodItems.eggplant_dolma))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModFoodItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModFoodItems.stuffed_pita))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModFoodItems.cooked_rice)),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModFoodItems.sushi_roll, 2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(new ItemStack(ModFoodItems.hand_pie))
          .showTooltip(true)
          .minBuildingLevel(4)
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
            new ItemStorage(new ItemStack(Items.MUTTON)),
            new ItemStorage(new ItemStack(Items.BOWL))))
          .result(new ItemStack(ModFoodItems.lamb_stew))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "borscht")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModFoodItems.chicken_broth)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.BEETROOT)),
            new ItemStorage(new ItemStack(Items.BEETROOT)),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(new ItemStack(ModFoodItems.borscht, 2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "fish_dinner")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.COD)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
          ))
          .result(new ItemStack(ModFoodItems.fish_dinner, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
        
        recipe(CHEF, MODULE_CRAFTING, "ramen")
          .inputs(List.of(
            new ItemStorage(new ItemStack(Items.KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModFoodItems.raw_noodle)),
            new ItemStorage(new ItemStack(ModFoodItems.soysauce)),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(new ItemStack(ModFoodItems.ramen, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "schnitzel")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModFoodItems.manchet_bread)),
            new ItemStorage(new ItemStack(Items.PORKCHOP)),
            new ItemStorage(new ItemStack(Items.EGG)),
            new ItemStorage(new ItemStack(Items.POTATO))
          ))
          .result(new ItemStack(ModFoodItems.schnitzel, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "steak_dinner")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BEEF)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.POTATO))
          ))
          .result(new ItemStack(ModFoodItems.steak_dinner, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "tacos")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModFoodItems.tortillas)),
            new ItemStorage(new ItemStack(Items.BEEF)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato))
          ))
          .result(new ItemStack(ModFoodItems.tacos, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "spicy_eggplant")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(new ItemStack(ModFoodItems.spicy_eggplant, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
    }
}
