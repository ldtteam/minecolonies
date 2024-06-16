package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.minecolonies.core.colony.events.ColonyExpeditionEvent.*;
import static com.minecolonies.core.generation.ExpeditionResourceManager.createEncounterLootItem;
import static com.minecolonies.core.generation.ExpeditionResourceManager.getEncounterId;

/**
 * Loot table generator for expeditions.
 */
public class DefaultExpeditionStructureLootProvider extends SimpleLootTableProvider
{
    public DefaultExpeditionStructureLootProvider(final PackOutput output)
    {
        super(output);
    }

    /**
     * Simple builder to automatically build a structure loot table.
     *
     * @param id        the id of the structure.
     * @param registrar the loot table registrar.
     * @param configure the further configuration handler.
     * @return the resource id of the loot table.
     */
    public ResourceLocation createStructureLootTable(final String id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final LootContextParamSet paramSet = LootContextParamSet.builder().build();

        final CompoundTag structureStart = new CompoundTag();
        structureStart.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START);
        structureStart.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, id);

        final CompoundTag structureEnd = new CompoundTag();
        structureStart.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END);
        structureStart.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, id);

        final Builder builder = new Builder();
        builder.withPool(new LootPool.Builder()
                           .setRolls(ConstantValue.exactly(1))
                           .add(LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureStart))));
        configure.accept(builder);
        builder.withPool(new LootPool.Builder()
                           .setRolls(ConstantValue.exactly(1))
                           .add(LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureEnd))));

        final ResourceLocation resId = new ResourceLocation(Constants.MOD_ID, "expeditions/structures/" + id);
        registrar.register(resId, paramSet, builder);
        return resId;
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
        createStructureLootTable("stronghold", registrar, this::withStrongholdLootTable);
    }
}
