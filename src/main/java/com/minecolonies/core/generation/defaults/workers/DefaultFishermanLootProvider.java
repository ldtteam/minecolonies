package com.minecolonies.core.generation.defaults.workers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecolonies.api.loot.EntityInBiomeTag;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.api.loot.ResearchUnlocked;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Datagen for fisherman loot tables
 */
public class DefaultFishermanLootProvider extends SimpleLootTableProvider
{
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();

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
                            @NotNull final ValidationContext validationtracker)
    {
        final ValidationContext newTracker = new ValidationContext(LootContextParamSets.ALL_PARAMS,
                conditionId -> null,
                lootId ->
                {
                    if (lootId.equals(BuiltInLootTables.FISHING_FISH) ||
                        lootId.equals(BuiltInLootTables.FISHING_JUNK) ||
                        lootId.equals(BuiltInLootTables.FISHING_TREASURE))
                    {
                        return LootTable.lootTable().build();
                    }
                    return validationtracker.resolveLootTable(lootId);
                });

        super.validate(map, newTracker);
    }

    private void registerStandardLoot(@NotNull final LootTableRegistrar registrar)
    {
        registrar.register(ModLootTables.FISHING, LootContextParamSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(ModLootTables.FISHING_JUNK).setWeight(10).setQuality(-2))
                        .add(LootTableReference.lootTableReference(ModLootTables.FISHING_TREASURE).setWeight(5).setQuality(2)
                                .when(new AlternativeLootItemCondition.Builder(
                                        EntityInBiomeTag.of(BiomeTags.IS_OCEAN),
                                        ResearchUnlocked.effect(ResearchConstants.FISH_TREASURE)
                                )))
                        .add(LootTableReference.lootTableReference(ModLootTables.FISHING_FISH).setWeight(85).setQuality(-1))
                ));

        registrar.register(ModLootTables.FISHING_JUNK, LootContextParamSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(BuiltInLootTables.FISHING_JUNK).setWeight(1))
                ));

        registrar.register(ModLootTables.FISHING_TREASURE, LootContextParamSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(BuiltInLootTables.FISHING_TREASURE).setWeight(1))
                ));

        registrar.register(ModLootTables.FISHING_FISH, LootContextParamSets.FISHING, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(BuiltInLootTables.FISHING_FISH).setWeight(1))
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
                LootItem.lootTableItem(Items.PRISMARINE_SHARD).setWeight(shardWeight).setQuality(skillBonus),
                LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(crystalWeight).setQuality(skillBonus)
        );

        final LootTable.Builder level45 = makeLoot(1000,
                LootItem.lootTableItem(Items.SPONGE).setWeight(spongeWeight).setQuality(skillBonus),
                LootItem.lootTableItem(Items.PRISMARINE_SHARD).setWeight(shardWeight).setQuality(skillBonus),
                LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(crystalWeight).setQuality(skillBonus)
        );

        registrar.register(ModLootTables.FISHERMAN_BONUS.get(1), LootContextParamSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(2), LootContextParamSets.EMPTY, noBonus);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(3), LootContextParamSets.ALL_PARAMS, level3);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(4), LootContextParamSets.ALL_PARAMS, level45);
        registrar.register(ModLootTables.FISHERMAN_BONUS.get(5), LootContextParamSets.ALL_PARAMS, level45);
    }

    private static LootTable.Builder makeLoot(int emptyWeight, @NotNull final LootPoolSingletonContainer.Builder<?>... entries)
    {
        final LootPool.Builder pool = LootPool.lootPool();

        for (final LootPoolSingletonContainer.Builder<?> entry : entries)
        {
            pool.add(entry);
            emptyWeight -= getWeightForEntry(entry);
        }

        pool.add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        return LootTable.lootTable().withPool(pool);
    }

    private static int getWeightForEntry(@NotNull final LootPoolSingletonContainer.Builder<?> entry)
    {
        // because it would be too easy for it to just have a public getter...
        final JsonObject json = GSON.toJsonTree(entry.build()).getAsJsonObject();
        return GsonHelper.getAsInt(json, "weight", 1);
    }
}
