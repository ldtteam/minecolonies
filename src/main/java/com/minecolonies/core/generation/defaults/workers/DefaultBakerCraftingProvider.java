package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
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
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3))))
                .result(ModItems.breadDough.toStack())
                .maxBuildingLevel(2)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "bread_dough3")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT)), new ItemStorage(ModItems.large_water_bottle.toStack())))
                .result(ModItems.breadDough.toStack(2))
                .minBuildingLevel(3)
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "bread")
                .inputs(List.of(new ItemStorage(ModItems.breadDough.toStack())))
                .result(new ItemStack(Items.BREAD))
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "sugary_bread")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
                        new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
                .result(ModItems.sugaryBread.toStack(4))
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
                .minBuildingLevel(3)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "milky_bread")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
                        new ItemStorage(ModItems.large_milk_bottle.toStack())))
                .result(ModItems.milkyBread.toStack(4))
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .minBuildingLevel(4)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "smilky_bread")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
                        new ItemStorage(ModItems.large_soy_milk_bottle.toStack())))
                .result(ModItems.milkyBread.toStack(4))
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .minBuildingLevel(4)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "golden_bread")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
                        new ItemStorage(new ItemStack(Items.GOLD_INGOT))))
                .result(ModItems.goldenBread.toStack(4))
                .minBuildingLevel(5)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "chorus_bread")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 8)),
                        new ItemStorage(new ItemStack(Items.CHORUS_FRUIT))))
                .result(ModItems.chorusBread.toStack(4))
                .minResearchId(ResearchConstants.THE_END)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "cookie_dough")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 2)),
                        new ItemStorage(new ItemStack(Items.COCOA_BEANS, 2))))
                .result(ModItems.cookieDough.toStack(8))
                .minBuildingLevel(2)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "cookie")
                .inputs(List.of(new ItemStorage(ModItems.cookieDough.toStack())))
                .result(new ItemStack(Items.COOKIE))
                .minBuildingLevel(2)
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "raw_pumpkin_pie")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.PUMPKIN)),
                        new ItemStorage(new ItemStack(Items.SUGAR)),
                        new ItemStorage(new ItemStack(Items.EGG))))
                .result(ModItems.rawPumpkinPie.toStack())
                .minBuildingLevel(3)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "pumpkin_pie")
                .inputs(List.of(new ItemStorage(ModItems.rawPumpkinPie.toStack())))
                .result(new ItemStack(Items.PUMPKIN_PIE))
                .minBuildingLevel(3)
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "cake_batter")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
                        new ItemStorage(ModItems.large_milk_bottle.toStack(3)),
                        new ItemStorage(new ItemStack(Items.SUGAR, 2)),
                        new ItemStorage(new ItemStack(Items.EGG))))
                .result(ModItems.cakeBatter.toStack())
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .minBuildingLevel(4)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "scake_batter")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.WHEAT, 3)),
                        new ItemStorage(ModItems.large_soy_milk_bottle.toStack(3)),
                        new ItemStorage(new ItemStack(Items.SUGAR, 2)),
                        new ItemStorage(new ItemStack(Items.EGG))))
                .result(ModItems.cakeBatter.toStack())
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .minBuildingLevel(4)
                .showTooltip(true)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "cake")
                .inputs(List.of(new ItemStorage(ModItems.cakeBatter.toStack())))
                .result(new ItemStack(Items.CAKE))
                .minBuildingLevel(4)
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "lembas_scone")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(ModItems.butter.toStack()),
                        new ItemStorage(new ItemStack(Items.HONEY_BOTTLE))))
                .result(ModItems.lembas_scone.toStack())
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GLASS_BOTTLE)
                .minBuildingLevel(1)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "manchet_dough")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(ModItems.butter.toStack())))
                .result(ModItems.manchet_dough.toStack(2))
                .minBuildingLevel(1)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "muffin_dough")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(ModItems.butter.toStack()),
                        new ItemStorage(new ItemStack(Items.SUGAR)),
                        new ItemStorage(new ItemStack(Items.SWEET_BERRIES))))
                .result(ModItems.muffin_dough.toStack(2))
                .minBuildingLevel(1)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "flatbread")
                .inputs(List.of(
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(new ItemStack(ModBlocks.blockDurum)),
                        new ItemStorage(ModItems.large_water_bottle.toStack())))
                .result(ModItems.flatbread.toStack())
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .minBuildingLevel(1)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "muffin")
                .inputs(List.of(new ItemStorage(ModItems.muffin_dough.toStack())))
                .result(ModItems.muffin.toStack())
                .minBuildingLevel(1)
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_SMELTING, "manchet")
                .inputs(List.of(new ItemStorage(ModItems.manchet_dough.toStack())))
                .result(ModItems.manchet_bread.toStack())
                .minBuildingLevel(1)
                .intermediate(Blocks.FURNACE)
                .build(consumer);

        recipe(BAKER, MODULE_CRAFTING, "water_jug")
                .inputs(List.of(new ItemStorage(ModItems.large_empty_bottle.toStack())))
                .result(ModItems.large_water_bottle.toStack())
                .build(consumer);
    }
}
