package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;
import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_SMELTING;

/**
 * Datagen for Baker
 */
public class DefaultBakerCraftingProvider extends CustomRecipeProvider
{
    private static final String BAKER = ModJobs.BAKER_ID.getPath();

    public DefaultBakerCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultBakerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        final ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "water_bottle")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.GLASS_BOTTLE))))
          .result(waterBottle)
          .minBuildingLevel(3)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "bread_dough")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3))))
          .result(new ItemStack(ModItems.breadDough))
          .maxBuildingLevel(2)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "bread_dough3")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT)), new ItemStorage(ModItems.large_water_bottle.getDefaultInstance())))
          .result(new ItemStack(ModItems.breadDough, 2))
          .minBuildingLevel(3)
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "bread")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.breadDough))))
          .result(new ItemStack(Items.BREAD))
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "sugary_bread")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
            new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
          .result(new ItemStack(ModItems.sugaryBread, 4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
          .minBuildingLevel(3)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "milky_bread")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
            new ItemStorage(new ItemStack(ModItems.large_milk_bottle))))
          .result(new ItemStack(ModItems.milkyBread, 4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "smilky_bread")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
            new ItemStorage(new ItemStack(ModItems.large_soy_milk_bottle))))
          .result(new ItemStack(ModItems.milkyBread, 4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "golden_bread")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
            new ItemStorage(new ItemStack(Items.GOLD_INGOT))))
          .result(new ItemStack(ModItems.goldenBread, 4))
          .minBuildingLevel(5)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "chorus_bread")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
            new ItemStorage(new ItemStack(Items.CHORUS_FRUIT))))
          .result(new ItemStack(ModItems.chorusBread, 4))
          .minResearchId(ResearchConstants.THE_END)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "cookie_dough")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 2)),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS, 2))))
          .result(new ItemStack(ModItems.cookieDough, 8))
          .minBuildingLevel(2)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "cookie")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.cookieDough))))
          .result(new ItemStack(Items.COOKIE))
          .minBuildingLevel(2)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "raw_pumpkin_pie")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.PUMPKIN)),
            new ItemStorage(new ItemStack(Items.SUGAR)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(new ItemStack(ModItems.rawPumpkinPie))
          .minBuildingLevel(3)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "pumpkin_pie")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.rawPumpkinPie))))
          .result(new ItemStack(Items.PUMPKIN_PIE))
          .minBuildingLevel(3)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "cake_batter")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
            new ItemStorage(new ItemStack(ModItems.large_milk_bottle, 3)),
            new ItemStorage(new ItemStack(Items.SUGAR, 2)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(new ItemStack(ModItems.cakeBatter))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "scake_batter")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
            new ItemStorage(new ItemStack(ModItems.large_soy_milk_bottle, 3)),
            new ItemStorage(new ItemStack(Items.SUGAR, 2)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(new ItemStack(ModItems.cakeBatter))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "cake")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.cakeBatter))))
          .result(new ItemStack(Items.CAKE))
          .minBuildingLevel(4)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "lembas_scone")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModItems.butter)),
            new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
          .result(new ItemStack(ModItems.lembas_scone))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "manchet_dough")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModItems.butter))))
          .result(new ItemStack(ModItems.manchet_dough, 2))
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "muffin_dough")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModItems.butter)),
            new ItemStorage(new ItemStack(Items.SUGAR)),
            new ItemStorage(new ItemStack(Items.SWEET_BERRIES))))
          .result(new ItemStack(ModItems.muffin_dough, 2))
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "flatbread")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(ModItems.large_water_bottle.getDefaultInstance())))
          .result(new ItemStack(ModItems.flatbread, 1))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "muffin")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.muffin_dough))))
          .result(new ItemStack(ModItems.muffin))
          .showTooltip(true)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_SMELTING, "manchet")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.manchet_dough))))
          .result(new ItemStack(ModItems.manchet_bread))
          .showTooltip(true)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "water_jug")
          .inputs(List.of(new ItemStorage(new ItemStack(ModItems.large_empty_bottle))))
          .result(ModItems.large_water_bottle.getDefaultInstance())
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "mintchoco_cheesecake")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModItems.plain_cheesecake)),
            new ItemStorage(new ItemStack(ModBlocks.blockMint)),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS)),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS))
          ))
          .result(new ItemStack(ModItems.mintchoco_cheesecake, 1))
          .minBuildingLevel(4)
          .build(consumer);

        CustomRecipeBuilder.create(BAKER, MODULE_CRAFTING, "mushroom_pizza")
          .inputs(List.of(
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockTomato)),
            new ItemStorage(new ItemStack(ModBlocks.blockGarlic)),
            new ItemStorage(new ItemStack(ModItems.cheddar_cheese)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
          ))
          .result(new ItemStack(ModItems.mushroom_pizza, 1))
          .minBuildingLevel(4)
          .build(consumer);
    }
}
