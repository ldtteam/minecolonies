package com.minecolonies.core.generation.defaults.workers;

import net.minecraft.data.loot.LootTableSubProvider;
import com.minecolonies.api.items.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.util.function.BiConsumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for generic recipe loot.  (This could be done in the individual crafter gens, but they're potentially
 * useful across multiple, and there's not very many of them.)
 */
public class DefaultRecipeLootProvider implements LootTableSubProvider
{
    public static final ResourceLocation LOOT_TABLE_LARGE_BOTTLE = new ResourceLocation(MOD_ID, "recipes/large_bottle");
    public static final ResourceLocation LOOT_TABLE_GRAVEL = new ResourceLocation(MOD_ID, "recipes/gravel");

    @Override
    public void generate(final BiConsumer<ResourceLocation, LootTable.Builder> generator)
    {
        registrar.register(LOOT_TABLE_LARGE_BOTTLE, LootContextParamSets.ALL_PARAMS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(100).setQuality(-1))
                        .add(LootItem.lootTableItem(ModItems.large_empty_bottle).setWeight(0).setQuality(1))));

        generator.accept(LOOT_TABLE_GRAVEL, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(90))
                        .add(LootItem.lootTableItem(Items.FLINT).setWeight(10))));
    }
}
