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
            new ItemStorage(ModFoodItems.manchet_bread.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(ModBlocks.blockCabbage.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack())))
          .result(ModFoodItems.stew_trencher.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pepper")
          .inputs(List.of(
            new ItemStorage(ModFoodItems.cooked_rice.toStack()),
            new ItemStorage(ModBlocks.blockBellPepper.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockEggplant.toStack())))
          .result(ModFoodItems.stuffed_pepper.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "pita_hummus")
          .inputs(List.of(
            new ItemStorage(ModFoodItems.flatbread.toStack()),
            new ItemStorage(ModBlocks.blockChickpea.toStack()),
            new ItemStorage(ModBlocks.blockChickpea.toStack()),
            new ItemStorage(ModBlocks.blockEggplant.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack())))
          .result(ModFoodItems.pita_hummus.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "eggplant_dolma")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockEggplant.toStack()),
            new ItemStorage(ModFoodItems.feta_cheese.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack())))
          .result(ModFoodItems.eggplant_dolma.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "stuffed_pita")
          .inputs(List.of(
            new ItemStorage(ModFoodItems.flatbread.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(ModBlocks.blockEggplant.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack())))
          .result(ModFoodItems.stuffed_pita.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "sushi_roll")
          .inputs(List.of(
            new ItemStorage(ModFoodItems.cooked_rice.toStack()),
            new ItemStorage(new ItemStack(Items.SALMON)),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(new ItemStack(Items.DRIED_KELP)),
            new ItemStorage(ModBlocks.blockOnion.toStack())))
          .result(ModFoodItems.sushi_roll.toStack(2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "hand_pie")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(new ItemStack(Items.MUTTON))))
          .result(ModFoodItems.hand_pie.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "lamb_stew")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.CARROT)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(ModBlocks.blockCabbage.toStack()),
            new ItemStorage(new ItemStack(Items.MUTTON)),
            new ItemStorage(new ItemStack(Items.BOWL))))
          .result(ModFoodItems.lamb_stew.toStack())
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "borscht")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(ModFoodItems.chicken_broth.toStack()),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.BEETROOT)),
            new ItemStorage(new ItemStack(Items.BEETROOT)),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(ModFoodItems.borscht.toStack(2))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "fish_dinner")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockCabbage.toStack()),
            new ItemStorage(new ItemStack(Items.COD)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
          ))
          .result(ModFoodItems.fish_dinner.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
        
        recipe(CHEF, MODULE_CRAFTING, "ramen")
          .inputs(List.of(
            new ItemStorage(new ItemStack(Items.KELP)),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(ModFoodItems.raw_noodle.toStack()),
            new ItemStorage(ModFoodItems.soysauce.toStack()),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(ModFoodItems.ramen.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "schnitzel")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModFoodItems.manchet_bread.toStack()),
            new ItemStorage(new ItemStack(Items.PORKCHOP)),
            new ItemStorage(new ItemStack(Items.EGG)),
            new ItemStorage(new ItemStack(Items.POTATO))
          ))
          .result(ModFoodItems.schnitzel.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "steak_dinner")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(new ItemStack(Items.BEEF)),
            new ItemStorage(new ItemStack(Items.POTATO)),
            new ItemStorage(new ItemStack(Items.POTATO))
          ))
          .result(ModFoodItems.steak_dinner.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "tacos")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockNetherPepper.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModFoodItems.tortillas.toStack()),
            new ItemStorage(new ItemStack(Items.BEEF)),
            new ItemStorage(ModBlocks.blockTomato.toStack())
          ))
          .result(ModFoodItems.tacos.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);

        recipe(CHEF, MODULE_CRAFTING, "spicy_eggplant")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockNetherPepper.toStack()),
            new ItemStorage(ModBlocks.blockNetherPepper.toStack()),
            new ItemStorage(ModBlocks.blockEggplant.toStack()),
            new ItemStorage(ModBlocks.blockEggplant.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModBlocks.blockOnion.toStack()),
            new ItemStorage(new ItemStack(Items.BOWL))
          ))
          .result(ModFoodItems.spicy_eggplant.toStack(1))
          .showTooltip(true)
          .minBuildingLevel(4)
          .build(consumer);
    }
}
