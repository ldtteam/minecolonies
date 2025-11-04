package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModFoodItems;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;
import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_SMELTING;

/**
 * Datagen for Baker
 */
public class DefaultBakerCraftingProvider extends CustomRecipeProvider
{
    private static final String BAKER = ModJobs.BAKER_ID.getPath();

    public DefaultBakerCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultBakerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        final ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);

        recipe(BAKER, MODULE_CRAFTING, "water_bottle")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.GLASS_BOTTLE))))
          .result(waterBottle)
          .minBuildingLevel(3)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "bread_dough")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT), 3)))
          .result(ModFoodItems.bread_dough.toStack())
          .maxBuildingLevel(2)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "bread_dough3")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT)), new ItemStorage(ModItems.largeWaterBottle.toStack())))
          .result(ModFoodItems.bread_dough.toStack(2))
          .minBuildingLevel(3)
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "bread")
          .inputs(List.of(new ItemStorage(ModFoodItems.bread_dough.toStack())))
          .result(new ItemStack(Items.BREAD))
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "sugary_bread")
          .inputs(List.of(new ItemStorage(ModBlocks.blockDurum.toStack(8)),
            new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
          .result(ModFoodItems.sugaryBread.toStack(4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
          .minBuildingLevel(3)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "milky_bread")
          .inputs(List.of(new ItemStorage(ModBlocks.blockDurum.toStack(8)),
            new ItemStorage(ModItems.largeMilkBottle.toStack())))
          .result(ModFoodItems.milkyBread.toStack(4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "smilky_bread")
          .inputs(List.of(new ItemStorage(ModBlocks.blockDurum.toStack(8)),
            new ItemStorage(ModItems.largeSoyMilkBottle.toStack())))
          .result(ModFoodItems.milkyBread.toStack(4))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "golden_bread")
          .inputs(List.of(new ItemStorage(ModBlocks.blockDurum.toStack(8)),
            new ItemStorage(new ItemStack(Items.GOLD_INGOT))))
          .result(ModFoodItems.goldenBread.toStack(4))
          .minBuildingLevel(5)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "chorus_bread")
          .inputs(List.of(new ItemStorage(ModBlocks.blockDurum.toStack(8)),
            new ItemStorage(new ItemStack(Items.CHORUS_FRUIT))))
          .result(ModFoodItems.chorusBread.toStack(4))
          .minResearchId(ResearchConstants.THE_END)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "cookie_dough")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 2)),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS, 2))))
          .result(ModFoodItems.cookie_dough.toStack(8))
          .minBuildingLevel(2)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "cookie")
          .inputs(List.of(new ItemStorage(ModFoodItems.cookie_dough.toStack())))
          .result(new ItemStack(Items.COOKIE))
          .minBuildingLevel(2)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "raw_pumpkin_pie")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.PUMPKIN)),
            new ItemStorage(new ItemStack(Items.SUGAR)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(ModFoodItems.raw_pumpkin_pie.toStack())
          .minBuildingLevel(3)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "pumpkin_pie")
          .inputs(List.of(new ItemStorage(ModFoodItems.raw_pumpkin_pie.toStack())))
          .result(new ItemStack(Items.PUMPKIN_PIE))
          .minBuildingLevel(3)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "cake_batter")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
            new ItemStorage(ModItems.largeMilkBottle.toStack(3)),
            new ItemStorage(new ItemStack(Items.SUGAR, 2)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(ModFoodItems.cake_batter.toStack())
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "scake_batter")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
            new ItemStorage(ModItems.largeSoyMilkBottle.toStack(3)),
            new ItemStorage(new ItemStack(Items.SUGAR, 2)),
            new ItemStorage(new ItemStack(Items.EGG))))
          .result(ModFoodItems.cake_batter.toStack())
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .minBuildingLevel(4)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "cake")
          .inputs(List.of(new ItemStorage(ModFoodItems.cake_batter.toStack())))
          .result(new ItemStack(Items.CAKE))
          .minBuildingLevel(4)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "lembas_scone")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModFoodItems.butter.toStack()),
            new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
          .result(ModFoodItems.lembas_scone.toStack())
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "manchet_dough")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModFoodItems.butter.toStack())))
          .result(ModFoodItems.manchet_dough.toStack(2))
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "muffin_dough")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModFoodItems.butter.toStack()),
            new ItemStorage(new ItemStack(Items.SUGAR)),
            new ItemStorage(new ItemStack(Items.SWEET_BERRIES))))
          .result(ModFoodItems.muffin_dough.toStack(2))
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "flatbread")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModItems.largeWaterBottle.toStack())))
          .result(ModFoodItems.flatbread.toStack(1))
          .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
          .showTooltip(true)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "muffin")
          .inputs(List.of(new ItemStorage(ModFoodItems.muffin_dough.toStack())))
          .result(ModFoodItems.muffin.toStack())
          .showTooltip(true)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "manchet")
          .inputs(List.of(new ItemStorage(ModFoodItems.manchet_dough.toStack())))
          .result(ModFoodItems.manchet_bread.toStack())
          .showTooltip(true)
          .intermediate(Blocks.FURNACE)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "water_jug")
          .inputs(List.of(new ItemStorage(ModItems.largeEmptyBottle.toStack())))
          .result(ModItems.largeWaterBottle.toStack())
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "mintchoco_cheesecake")
          .inputs(List.of(
            new ItemStorage(ModFoodItems.plain_cheesecake.toStack()),
            new ItemStorage(ModBlocks.blockMint.toStack()),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS)),
            new ItemStorage(new ItemStack(Items.COCOA_BEANS))
          ))
          .result(ModFoodItems.mintchoco_cheesecake.toStack(1))
          .minBuildingLevel(4)
          .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "mushroom_pizza")
          .inputs(List.of(
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockDurum.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(ModBlocks.blockTomato.toStack()),
            new ItemStorage(ModBlocks.blockGarlic.toStack()),
            new ItemStorage(ModFoodItems.cheddar_cheese.toStack()),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM)),
            new ItemStorage(new ItemStack(Items.BROWN_MUSHROOM))
          ))
          .result(ModFoodItems.mushroom_pizza.toStack(1))
          .minBuildingLevel(4)
          .build(consumer);
    }
}
