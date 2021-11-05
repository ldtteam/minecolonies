package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import org.jetbrains.annotations.NotNull;

/**
 * Datagen for fisherman bonus loot (in addition to normal fishing loot tables)
 */
public class DefaultFishermanBonusProvider extends SimpleLootTableProvider
{
    public DefaultFishermanBonusProvider(@NotNull final DataGenerator dataGeneratorIn)
    {
        super(dataGeneratorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultFishermanBonusProvider";
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        final LootTable.Builder noBonus = LootTable.lootTable();

        final int spongeWeight = 1;     // 0.1% chance (before skill bonus)
        final int shardWeight = 25;     // 2.5%
        final int crystalWeight = 25;   // 2.5%
        final int emptyWeight = 1000 - spongeWeight - shardWeight - crystalWeight;
        final int skillBonus = 1;       // 0.1% bonus chance per skill point (sort of)
        // skill bonus is a simple linear scaling, so the effective weights are:
        //               skill = 0   skill = 50   skill = 100
        //     sponge:    1 (0.1%)   51 (4.43%)   101 (7.77%)
        //      shard:   25 (2.5%)   75 (6.52%)   125 (9.61%)
        //    crystal:   25 (2.5%)   75 (6.52%)   125 (9.61%)
        //    nothing:   949 (95%)   949 (82%)    949 (73%)
        // at most one item will drop from the bonus table (unless you add extra rolls/pools)
        // but this is in addition to whatever they fish up using the regular fishing loot.

        final LootTable.Builder standardBonus = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .add(ItemLootEntry.lootTableItem(Items.SPONGE).setWeight(spongeWeight).setQuality(skillBonus))
                    .add(ItemLootEntry.lootTableItem(Items.PRISMARINE_SHARD).setWeight(shardWeight).setQuality(skillBonus))
                    .add(ItemLootEntry.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(crystalWeight).setQuality(skillBonus))
                    .add(EmptyLootEntry.emptyItem().setWeight(emptyWeight))
                );

        registrar.register(ModLootTables.FISHERMAN_BONUS.get(1), LootParameterSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(2), LootParameterSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(3), LootParameterSets.ALL_PARAMS, standardBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(4), LootParameterSets.ALL_PARAMS, standardBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(5), LootParameterSets.ALL_PARAMS, standardBonus);
    }
}
