package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.component.ModDataComponents;
import com.minecolonies.api.items.component.SupplyData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Loot table generator for supply camp/ship
 */
public class DefaultSupplyLootProvider implements LootTableSubProvider
{
    /**
     * Resource locations, path and names must fit the existing json file.
     */
    public final static ResourceLocation SUPPLY_CAMP_LT = new ResourceLocation(MOD_ID, "chests/supplycamp");
    public final static ResourceLocation SUPPLY_SHIP_LT = new ResourceLocation(MOD_ID, "chests/supplyship");

    public DefaultSupplyLootProvider(@NotNull final HolderLookup.Provider provider)
    {
    }

    @Override
    public void generate(final BiConsumer<ResourceKey<LootTable>, Builder> generator)
    {
        final CompoundTag instantTag = new CompoundTag();
        instantTag.putString(PLACEMENT_NBT, INSTANT_PLACEMENT);

        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_CAMP_LT),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(ModItems.supplyCamp)
                                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                        .apply(SetComponentsFunction.setComponent(ModDataComponents.SUPPLY_COMPONENT.value(), new SupplyData(false, true, -1)))
                                        .apply(SetNameFunction.setName(Component.translatableEscape("item.minecolonies.supply.free", ModItems.supplyCamp.getDescription()), SetNameFunction.Target.ITEM_NAME)))
                                .add(LootItem.lootTableItem(ModItems.scrollBuff)
                                        .when(LootItemRandomChanceCondition.randomChance(0.2f))
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))))
                        ));

        generator.accept(ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_SHIP_LT),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(ModItems.supplyChest)
                                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                       .apply(SetComponentsFunction.setComponent(ModDataComponents.SUPPLY_COMPONENT.value(), new SupplyData(false, true, -1)))
                                        .apply(SetNameFunction.setName(Component.translatableEscape("item.minecolonies.supply.free", ModItems.supplyChest.getDescription()), SetNameFunction.Target.ITEM_NAME)))
                                .add(LootItem.lootTableItem(ModItems.scrollBuff)
                                        .when(LootItemRandomChanceCondition.randomChance(0.2f))
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))))
                        ));
    }
}
