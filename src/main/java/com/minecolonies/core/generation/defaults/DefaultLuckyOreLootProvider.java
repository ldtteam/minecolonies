package com.minecolonies.core.generation.defaults;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootPool.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.LUCKY_ORE_LOOT_TABLE;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.LUCKY_ORE_PARAM_SET;

/**
 * Loot table generator for lucky ores.
 */
public class DefaultLuckyOreLootProvider implements LootTableSubProvider
{
    private final HolderLookup.Provider provider;

    public DefaultLuckyOreLootProvider(@NotNull final HolderLookup.Provider provider)
    {
        this.provider = provider;
    }

    @Override
    public void generate(@NotNull final BiConsumer<ResourceKey<LootTable>, LootTable.Builder> generator)
    {
        final LootPool.Builder luckyOres1 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48));
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(1))), LootTable.lootTable().withPool(luckyOres1).setParamSet(LUCKY_ORE_PARAM_SET));

        final LootPool.Builder luckyOres2 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16));
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(2))), LootTable.lootTable().withPool(luckyOres2).setParamSet(LUCKY_ORE_PARAM_SET));

        final LootPool.Builder luckyOres3 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16))
                                              .add(LootItem.lootTableItem(Items.REDSTONE_ORE).setWeight(8))
                                              .add(LootItem.lootTableItem(Items.LAPIS_ORE).setWeight(4));
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(3))), LootTable.lootTable().withPool(luckyOres3).setParamSet(LUCKY_ORE_PARAM_SET));

        final LootPool.Builder luckyOres4 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16))
                                              .add(LootItem.lootTableItem(Items.REDSTONE_ORE).setWeight(8))
                                              .add(LootItem.lootTableItem(Items.LAPIS_ORE).setWeight(4))
                                              .add(LootItem.lootTableItem(Items.DIAMOND_ORE).setWeight(2))
                                              .add(LootItem.lootTableItem(Items.EMERALD_ORE).setWeight(1));
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(4))), LootTable.lootTable().withPool(luckyOres4).setParamSet(LUCKY_ORE_PARAM_SET));
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(5))), LootTable.lootTable().withPool(luckyOres4).setParamSet(LUCKY_ORE_PARAM_SET));
    }
}
