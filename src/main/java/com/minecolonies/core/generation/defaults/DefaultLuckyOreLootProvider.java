package com.minecolonies.core.generation.defaults;

import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootPool.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.LUCKY_ORE_LOOT_TABLE;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.LUCKY_ORE_PARAM_SET;

/**
 * Loot table generator for lucky ores.
 */
public class DefaultLuckyOreLootProvider extends SimpleLootTableProvider
{
    public DefaultLuckyOreLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Lucky Ores Loot Table Provider";
    }

    @Override
    protected void registerTables(final @NotNull SimpleLootTableProvider.LootTableRegistrar registrar)
    {
        final LootPool.Builder luckyOres1 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48));
        registrar.register(LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(1)), LUCKY_ORE_PARAM_SET, LootTable.lootTable().withPool(luckyOres1));

        final LootPool.Builder luckyOres2 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16));
        registrar.register(LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(2)), LUCKY_ORE_PARAM_SET, LootTable.lootTable().withPool(luckyOres2));

        final LootPool.Builder luckyOres3 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16))
                                              .add(LootItem.lootTableItem(Items.REDSTONE_ORE).setWeight(8))
                                              .add(LootItem.lootTableItem(Items.LAPIS_ORE).setWeight(4));
        registrar.register(LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(3)), LUCKY_ORE_PARAM_SET, LootTable.lootTable().withPool(luckyOres3));

        final LootPool.Builder luckyOres4 = new Builder()
                                              .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(64))
                                              .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(48))
                                              .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(32))
                                              .add(LootItem.lootTableItem(Items.GOLD_ORE).setWeight(16))
                                              .add(LootItem.lootTableItem(Items.REDSTONE_ORE).setWeight(8))
                                              .add(LootItem.lootTableItem(Items.LAPIS_ORE).setWeight(4))
                                              .add(LootItem.lootTableItem(Items.DIAMOND_ORE).setWeight(2))
                                              .add(LootItem.lootTableItem(Items.EMERALD_ORE).setWeight(1));
        registrar.register(LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(4)), LUCKY_ORE_PARAM_SET, LootTable.lootTable().withPool(luckyOres4));
        registrar.register(LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(5)), LUCKY_ORE_PARAM_SET, LootTable.lootTable().withPool(luckyOres4));
    }
}
