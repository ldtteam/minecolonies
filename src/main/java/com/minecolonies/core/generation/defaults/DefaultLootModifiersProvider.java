package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.loot.GenerateSupplyLoot;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.core.generation.defaults.DefaultCropsLootProvider.DUNGEON_CROPS;
import static com.minecolonies.core.generation.defaults.DefaultSupplyLootProvider.SUPPLY_CAMP_LT;
import static com.minecolonies.core.generation.defaults.DefaultSupplyLootProvider.SUPPLY_SHIP_LT;

/**
 * NeoForge global loot table modifier datagen.
 */
public class DefaultLootModifiersProvider extends GlobalLootModifierProvider
{
    public DefaultLootModifiersProvider(@NotNull final PackOutput output,
                                        @NotNull final CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries, MOD_ID);
    }

    @Override
    protected void start()
    {
        addCrops();
        addSupplies();
    }

    /**
     * Adds crop drops to world blocks.
     */
    private void addCrops()
    {
        final Set<ResourceKey<LootTable>> cropSources = new HashSet<>();
        for (final MinecoloniesCropBlock crop : ModBlocks.getCrops())
        {
            for (final Block source : crop.getDroppedFrom())
            {
                cropSources.add(source.getLootTable());
            }
        }

        for (final ResourceKey<LootTable> source : cropSources)
        {
            final ResourceKey<LootTable> cropTable = DefaultCropsLootProvider.getCropSourceLootTable(source.location());
            add(cropTable.location().getPath(),
                    new AddTableLootModifier(new LootItemCondition[] { forLootTable(source).build() }, cropTable),
                    new ModLoadedCondition(MOD_ID));
        }

        add(DUNGEON_CROPS.location().getPath(),
                new AddTableLootModifier(new LootItemCondition[] { forLootTable(BuiltInLootTables.SIMPLE_DUNGEON).build() }, DUNGEON_CROPS));
    }

    /**
     * Adds supply chest/camps to dungeon loot.
     */
    private void addSupplies()
    {
        final List<ResourceKey<LootTable>> campTables = List.of(
                BuiltInLootTables.SPAWN_BONUS_CHEST,
                BuiltInLootTables.SIMPLE_DUNGEON,
                BuiltInLootTables.VILLAGE_CARTOGRAPHER,
                BuiltInLootTables.VILLAGE_MASON,
                BuiltInLootTables.VILLAGE_DESERT_HOUSE,
                BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
                BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
                BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
                BuiltInLootTables.ABANDONED_MINESHAFT,
                BuiltInLootTables.STRONGHOLD_LIBRARY,
                BuiltInLootTables.STRONGHOLD_CROSSING,
                BuiltInLootTables.STRONGHOLD_CORRIDOR,
                BuiltInLootTables.DESERT_PYRAMID,
                BuiltInLootTables.JUNGLE_TEMPLE,
                BuiltInLootTables.IGLOO_CHEST,
                BuiltInLootTables.WOODLAND_MANSION,
                BuiltInLootTables.PILLAGER_OUTPOST
        );

        final List<ResourceKey<LootTable>> shipTables = List.of(
                BuiltInLootTables.UNDERWATER_RUIN_SMALL,
                BuiltInLootTables.UNDERWATER_RUIN_BIG,
                BuiltInLootTables.BURIED_TREASURE,
                BuiltInLootTables.SHIPWRECK_MAP,
                BuiltInLootTables.SHIPWRECK_SUPPLY,
                BuiltInLootTables.SHIPWRECK_TREASURE,
                BuiltInLootTables.VILLAGE_FISHER,
                BuiltInLootTables.VILLAGE_ARMORER,
                BuiltInLootTables.VILLAGE_TEMPLE
        );

        add("supplycamp_loot",
                new AddTableLootModifier(new LootItemCondition[] { GenerateSupplyLoot.when().build(), forLootTables(campTables).build() },
                ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_CAMP_LT)));

        add("supplyship_loot",
                new AddTableLootModifier(new LootItemCondition[] { GenerateSupplyLoot.when().build(), forLootTables(shipTables).build() },
                ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_SHIP_LT)));
    }

    private static LootItemCondition.Builder forLootTable(@NotNull final ResourceKey<LootTable> table)
    {
        return LootTableIdCondition.builder(table.location());
    }

    private static LootItemCondition.Builder forLootTables(@NotNull final Collection<ResourceKey<LootTable>> tables)
    {
        return AnyOfCondition.anyOf(tables.stream()
                .map(t -> LootTableIdCondition.builder(t.location()))
                .toArray(LootItemCondition.Builder[]::new));
    }
}
