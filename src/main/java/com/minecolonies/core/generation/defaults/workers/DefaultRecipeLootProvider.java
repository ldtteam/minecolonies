package com.minecolonies.core.generation.defaults.workers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import com.minecolonies.api.items.ModItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for generic recipe loot.  (This could be done in the individual crafter gens, but they're potentially
 * useful across multiple, and there's not very many of them.)
 */
public class DefaultRecipeLootProvider implements LootTableSubProvider
{
    public DefaultRecipeLootProvider(@NotNull final HolderLookup.Provider provider)
    {

    }

    public static final ResourceLocation LOOT_TABLE_LARGE_BOTTLE = new ResourceLocation(MOD_ID, "recipes/large_bottle");
    public static final ResourceLocation LOOT_TABLE_GRAVEL = new ResourceLocation(MOD_ID, "recipes/gravel");

    @Override
    public void generate(final BiConsumer<ResourceKey<LootTable>, LootTable.Builder> generator)
    {
        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LOOT_TABLE_LARGE_BOTTLE), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(100).setQuality(-1))
                        .add(LootItem.lootTableItem(ModItems.large_empty_bottle).setWeight(0).setQuality(1))));

        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, LOOT_TABLE_GRAVEL), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(EmptyLootItem.emptyItem().setWeight(90))
                        .add(LootItem.lootTableItem(Items.FLINT).setWeight(10))));
    }
}
