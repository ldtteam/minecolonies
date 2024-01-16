package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Loot table generator for supply camp/ship
 */
public class DefaultSupplyLootProvider extends SimpleLootTableProvider
{
    public DefaultSupplyLootProvider(@NotNull PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Supplies Loot Table Provider";
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        final CompoundTag instantTag = new CompoundTag();
        instantTag.putString(PLACEMENT_NBT, INSTANT_PLACEMENT);

        registrar.register(new ResourceLocation(MOD_ID, "chests/supplycamp"), LootContextParamSets.CHEST,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(ModItems.supplyCamp)
                                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                        .apply(SetNbtFunction.setTag(instantTag))
                                        .apply(SetNameFunction.setName(Component.translatable("item.minecolonies.supply.free", ModItems.supplyCamp.getDescription()))))
                                .add(LootItem.lootTableItem(ModItems.scrollBuff)
                                        .when(LootItemRandomChanceCondition.randomChance(0.2f))
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))))
                        ));

        registrar.register(new ResourceLocation(MOD_ID, "chests/supplyship"), LootContextParamSets.CHEST,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(ModItems.supplyChest)
                                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                        .apply(SetNbtFunction.setTag(instantTag))
                                        .apply(SetNameFunction.setName(Component.translatable("item.minecolonies.supply.free", ModItems.supplyChest.getDescription()))))
                                .add(LootItem.lootTableItem(ModItems.scrollBuff)
                                        .when(LootItemRandomChanceCondition.randomChance(0.2f))
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8))))
                        ));
    }
}
