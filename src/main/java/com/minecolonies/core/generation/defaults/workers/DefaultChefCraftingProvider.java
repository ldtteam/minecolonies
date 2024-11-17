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
        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stew_trencher")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.manchet_bread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.stew_trencher))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stuffed_pepper")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(ModBlocks.blockBellPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant))))
          .result(new ItemStack(ModItems.stuffed_pepper))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "pita_hummus")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockChickpea)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.pita_hummus))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "eggplant_dolma")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModItems.feta_cheese)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.eggplant_dolma))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.flatbread)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic))))
          .result(new ItemStack(ModItems.stuffed_pita))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.cooked_rice)),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion))))
          .result(new ItemStack(ModItems.sushi_roll, 2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(new ItemStack(ModItems.hand_pie))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "lamb_stew")
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
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "borscht")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModItems.chicken_broth)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.BEETROOT)),
            new ItemStorage(new ItemStack(Items.BEETROOT))
          ))
          .result(new ItemStack(ModItems.borscht, 2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "fish_dinner")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockCabbage)),
            new ItemStorage(new ItemStack(Items.COD)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
          ))
          .result(new ItemStack(ModItems.fish_dinner, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
        
        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "ramen")
          .inputs(List.of(
            new ItemStorage(new ItemStack(Items.KELP)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(ModItems.raw_noodle)),
            new ItemStorage(new ItemStack(ModItems.soysauce))
          ))
          .result(new ItemStack(ModItems.ramen, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
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
          .showTooltip(true)
          .minBuildingLevel(4)
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
          .showTooltip(true)
          .minBuildingLevel(4)
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
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeBuilder.create(CHEF, MODULE_CRAFTING, "spicy_eggplant")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockNetherPepper)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockEggplant)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModBlocks.blockOnion)),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(new ItemStack(ModItems.spicy_eggplant, 1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
    }
}
