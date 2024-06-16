package com.minecolonies.core.generation.defaults;

import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.minecolonies.core.generation.ExpeditionResourceManager.*;

/**
 * Loot table generator for expeditions.
 */
public class DefaultExpeditionStructureLootProvider extends SimpleLootTableProvider
{
    /**
     * Expedition structure constants.
     */
    public static final String STRONGHOLD_ID = "stronghold";

    /**
     * Default constructor.
     */
    public DefaultExpeditionStructureLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Structure Loot";
    }

    /**
     * Simple builder to automatically build a structure loot table.
     *
     * @param id        the id of the structure.
     * @param registrar the loot table registrar.
     * @param configure the further configuration handler.
     */
    public void createStructureLootTable(final String id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final LootContextParamSet paramSet = LootContextParamSet.builder().build();

        final Builder builder = new Builder();
        builder.withPool(new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(createStructureStartItem(id)));
        configure.accept(builder);
        builder.withPool(new LootPool.Builder().setRolls(ConstantValue.exactly(1)).add(createStructureEndItem(id)));

        registrar.register(getStructureId(id), paramSet, builder);
    }

    /**
     * Loot table builder for the stronghold.
     */
    private void withStrongholdLootTable(final Builder builder)
    {
        builder.withPool(new LootPool.Builder()
                           .setRolls(UniformGenerator.between(2, 6))
                           .add(createEncounterLootItem(getEncounterId("zombie")).setWeight(50).setQuality(-10))
                           .add(createEncounterLootItem(getEncounterId("skeleton")).setWeight(30).setQuality(-10))
                           .add(createEncounterLootItem(getEncounterId("creeper")).setWeight(10).setQuality(-15))
                           .add(createEncounterLootItem(getEncounterId("enderman")).setWeight(10).setQuality(-20)));
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createStructureLootTable(STRONGHOLD_ID, registrar, this::withStrongholdLootTable);
    }
}
