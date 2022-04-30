package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.loot.ModLootContextParamSets;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import com.minecolonies.coremod.items.ItemArcheologistLoot;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public class DefaultArcheologistsLootTableProvider extends SimpleLootTableProvider
{
    public DefaultArcheologistsLootTableProvider(final @NotNull DataGenerator dataGenerator)
    {
        super(dataGenerator);
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        registerDefaultLootPool(registrar);
        registerStructureLootPool(registrar, StructureFeature.DESERT_PYRAMID);
        registerStructureLootPool(registrar, StructureFeature.JUNGLE_TEMPLE);
    }

    private void registerDefaultLootPool(final LootTableRegistrar registrar) {
        registrar.register(
          ModLootTables.ARCHEOLOGISTS_DEFAULT_LOOT_TABLE,
          ModLootContextParamSets.CITIZEN_PERFORMS_LOOTING,
          new LootTable.Builder().setParamSet(ModLootContextParamSets.CITIZEN_PERFORMS_LOOTING)
            .withPool(
              buildDefaultLootPool(new LootPool.Builder()
                .setRolls(UniformGenerator.between(0, 2))
                .setBonusRolls(UniformGenerator.between(-1, 1))
              )
            )
        );
    }

    private void registerStructureLootPool(final LootTableRegistrar registrar, final StructureFeature<?> feature) {
        registrar.register(
          new ResourceLocation(Constants.MOD_ID, "archeologist/" + Objects.requireNonNull(feature.getRegistryName()).getNamespace() + "/" + feature.getRegistryName().getPath()),
          ModLootContextParamSets.CITIZEN_PERFORMS_LOOTING,
          new LootTable.Builder().setParamSet(ModLootContextParamSets.CITIZEN_PERFORMS_LOOTING)
            .withPool(
              buildStructureLootPool(new LootPool.Builder()
                .setRolls(UniformGenerator.between(0, 2))
                .setBonusRolls(UniformGenerator.between(-1, 1)),
                feature
              )
            )
        );
    }

    private LootPool.Builder buildDefaultLootPool(final LootPool.Builder builder)
    {
        buildLootPool(builder, ModItems.archeologistLootItems.stream()
          .filter(ItemArcheologistLoot.class::isInstance)
          .map(ItemArcheologistLoot.class::cast));
        return builder;
    }

    private LootPool.Builder buildStructureLootPool(final LootPool.Builder builder, final StructureFeature<?> feature)
    {
        buildLootPool(builder, ModItems.archeologistLootItems.stream()
          .filter(ItemArcheologistLoot.class::isInstance)
          .map(ItemArcheologistLoot.class::cast)
          .filter(item -> item.getStructureFeature() == feature));
        return builder;
    }

    private void buildLootPool(final LootPool.Builder builder, Stream<ItemArcheologistLoot> archeologistItem)
    {
        archeologistItem.forEach(loot -> addArcheologistLoot(builder, loot));
    }

    private void addArcheologistLoot(final LootPool.Builder builder, final ItemArcheologistLoot loot) {
        builder.add(createArcheologistLoot(loot));
    }

    private LootPoolSingletonContainer.Builder<?> createArcheologistLoot(final ItemArcheologistLoot loot) {
        return LootItem.lootTableItem(loot).setWeight(getWeight(loot)).setQuality(getQuality(loot));
    }

    private int getWeight(final ItemArcheologistLoot loot) {
        return switch (loot.getType()) {
            case SMALL -> 25;
            case LARGE -> 10;
            case TABLET -> 1;
        };
    }

    private int getQuality(final ItemArcheologistLoot loot) {
        return switch (loot.getDegradationLevel()) {
            case EXTREMELY -> -25;
            case SEVERELY -> -10;
            case MODERATELY -> 0;
            case SLIGHTLY -> 10;
            case NONE -> 25;
        };
    }
}
