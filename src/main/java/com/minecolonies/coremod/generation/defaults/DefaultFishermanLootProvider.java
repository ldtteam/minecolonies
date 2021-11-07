package com.minecolonies.coremod.generation.defaults;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecolonies.api.loot.EntityInBiomeCategory;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Datagen for fisherman loot tables
 */
public class DefaultFishermanLootProvider extends SimpleLootTableProvider
{
    private static final Gson GSON = LootSerializers.createLootTableSerializer().create();

    public DefaultFishermanLootProvider(@NotNull final DataGenerator dataGeneratorIn)
    {
        super(dataGeneratorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultFishermanLootProvider";
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        registerStandardLoot(registrar);
        registerBonusLoot(registrar);
    }

    @Override
    protected void validate(@NotNull final Map<ResourceLocation, LootTable> map,
                            @NotNull final ValidationTracker validationtracker)
    {
        final ValidationTracker newTracker = new ValidationTracker(LootParameterSets.ALL_PARAMS,
                conditionId -> null,
                lootId ->
                {
                    if (lootId.equals(LootTables.FISHING_FISH) ||
                        lootId.equals(LootTables.FISHING_JUNK) ||
                        lootId.equals(LootTables.FISHING_TREASURE))
                    {
                        return LootTable.lootTable().build();
                    }
                    return validationtracker.resolveLootTable(lootId);
                });

        super.validate(map, newTracker);
    }

    private void registerStandardLoot(@NotNull final LootTableRegistrar registrar)
    {
        registrar.register(ModLootTables.FISHING, LootParameterSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(TableLootEntry.lootTableReference(ModLootTables.FISHING_JUNK).setWeight(10).setQuality(-2))
                        .add(TableLootEntry.lootTableReference(ModLootTables.FISHING_TREASURE).setWeight(5).setQuality(2)
                                .when(EntityInBiomeCategory.of(Biome.Category.OCEAN)))
                        .add(TableLootEntry.lootTableReference(ModLootTables.FISHING_FISH).setWeight(85).setQuality(-1))
                ));

        registrar.register(ModLootTables.FISHING_JUNK, LootParameterSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(TableLootEntry.lootTableReference(LootTables.FISHING_JUNK).setWeight(1))
                ));

        registrar.register(ModLootTables.FISHING_TREASURE, LootParameterSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(TableLootEntry.lootTableReference(LootTables.FISHING_TREASURE).setWeight(1))
                ));

        registrar.register(ModLootTables.FISHING_FISH, LootParameterSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(TableLootEntry.lootTableReference(LootTables.FISHING_FISH).setWeight(1))
                ));
    }

    private void registerBonusLoot(@NotNull final LootTableRegistrar registrar)
    {
        final int skillBonus = 1;       // 0.1% bonus chance per skill point (sort of)
        final int spongeWeight = 1;     // 0.1% chance (before skill bonus)
        final int shardWeight = 25;     // 2.5%
        final int crystalWeight = 25;   // 2.5%
        // skill bonus is a simple linear scaling, so the effective weights are:
        //               skill = 0   skill = 50   skill = 100
        //     sponge:    1 (0.1%)   51 (4.43%)   101 (7.77%)
        //      shard:   25 (2.5%)   75 (6.52%)   125 (9.61%)
        //    crystal:   25 (2.5%)   75 (6.52%)   125 (9.61%)
        //    nothing:   949 (95%)   949 (82%)    949 (73%)
        // at most one item will drop from the bonus table (unless you add extra rolls/pools)
        // but this is in addition to whatever they fish up using the regular fishing loot.

        final LootTable.Builder noBonus = LootTable.lootTable();

        final LootTable.Builder level3 = makeLoot(1000,
                ItemLootEntry.lootTableItem(Items.PRISMARINE_SHARD).setWeight(shardWeight).setQuality(skillBonus),
                ItemLootEntry.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(crystalWeight).setQuality(skillBonus)
        );

        final LootTable.Builder level45 = makeLoot(1000,
                ItemLootEntry.lootTableItem(Items.SPONGE).setWeight(spongeWeight).setQuality(skillBonus),
                ItemLootEntry.lootTableItem(Items.PRISMARINE_SHARD).setWeight(shardWeight).setQuality(skillBonus),
                ItemLootEntry.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(crystalWeight).setQuality(skillBonus)
        );

        registrar.register(ModLootTables.FISHERMAN_BONUS.get(1), LootParameterSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(2), LootParameterSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(3), LootParameterSets.ALL_PARAMS, level3);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(4), LootParameterSets.ALL_PARAMS, level45);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(5), LootParameterSets.ALL_PARAMS, level45);
    }

    private static LootTable.Builder makeLoot(int emptyWeight, @NotNull final StandaloneLootEntry.Builder<?>... entries)
    {
        final LootPool.Builder pool = LootPool.lootPool();

        for (final StandaloneLootEntry.Builder<?> entry : entries)
        {
            pool.add(entry);
            emptyWeight -= getWeightForEntry(entry);
        }

        pool.add(EmptyLootEntry.emptyItem().setWeight(emptyWeight));
        return LootTable.lootTable().withPool(pool);
    }

    private static int getWeightForEntry(@NotNull final StandaloneLootEntry.Builder<?> entry)
    {
        // because it would be too easy for it to just have a public getter...
        final JsonObject json = GSON.toJsonTree(entry.build()).getAsJsonObject();
        return JSONUtils.getAsInt(json, "weight", 1);
    }
}
