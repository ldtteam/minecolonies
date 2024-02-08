package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Loot table generator for entities
 */
public class DefaultEntityLootProvider extends SimpleLootTableProvider
{
    public DefaultEntityLootProvider(@NotNull PackOutput packOutput)
    {
        super(packOutput);
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        registerLoot(registrar, ModEntities.AMAZON, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(15))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.AMAZONSPEARMAN, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(ModItems.spear).setWeight(15))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.AMAZONCHIEF, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(80))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(30)));

        registerLoot(registrar, ModEntities.BARBARIAN, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.DIAMOND_AXE).setWeight(1))
                .add(LootItem.lootTableItem(Items.GOLDEN_AXE).setWeight(2))
                .add(LootItem.lootTableItem(Items.IRON_AXE).setWeight(5))
                .add(LootItem.lootTableItem(Items.STONE_AXE).setWeight(6))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(3)));

        registerLoot(registrar, ModEntities.ARCHERBARBARIAN, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.CHIEFBARBARIAN, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(50))
                .add(LootItem.lootTableItem(ModItems.chiefSword).setWeight(1).setQuality(1))
                .add(LootItem.lootTableItem(Items.DIAMOND_SWORD).setWeight(5))
                .add(LootItem.lootTableItem(Items.GOLDEN_SWORD).setWeight(5))
                .add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(10))
                .add(LootItem.lootTableItem(Items.STONE_SWORD).setWeight(20))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(30)));

        registerLoot(registrar, ModEntities.SHIELDMAIDEN, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.SHIELD).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.NORSEMEN_ARCHER, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.NORSEMEN_CHIEF, builder -> builder
                .setRolls(ConstantValue.exactly(2))
                .add(EmptyLootItem.emptyItem().setWeight(50))
                .add(LootItem.lootTableItem(Items.LEATHER).setWeight(15).setQuality(5))
                .add(LootItem.lootTableItem(Items.DIAMOND_AXE).setWeight(10).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(50)));

        registerLoot(registrar, ModEntities.PIRATE, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(ModItems.scimitar).setWeight(6))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(4)));

        registerLoot(registrar, ModEntities.ARCHERPIRATE, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.CHIEFPIRATE, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(50))
                .add(LootItem.lootTableItem(ModItems.pirateHelmet_1).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateLegs_1).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateBoots_1).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateChest_1).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateHelmet_2).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateLegs_2).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateBoots_2).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.pirateChest_2).setWeight(5).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.scimitar).setWeight(25).setQuality(1))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(30)));

        registerLoot(registrar, ModEntities.MUMMY, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(15)));

        registerLoot(registrar, ModEntities.ARCHERMUMMY, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(80))
                .add(LootItem.lootTableItem(Items.BOW).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(5)));

        registerLoot(registrar, ModEntities.PHARAO, builder -> builder
                .add(EmptyLootItem.emptyItem().setWeight(50))
                .add(LootItem.lootTableItem(ModItems.pharaoscepter).setWeight(3).setQuality(1))
                .add(LootItem.lootTableItem(Items.ARROW).setWeight(20)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16)))
                        .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(1, 32))))
                .add(LootItem.lootTableItem(ModItems.firearrow).setWeight(10)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 16)))
                        .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(1, 32))))
                .add(LootItem.lootTableItem(ModItems.ancientTome).setWeight(30)));
    }

    private void registerLoot(@NotNull final LootTableRegistrar registrar,
                              @NotNull final EntityType<?> entity,
                              @NotNull final Consumer<LootPool.Builder> builder)
    {
        final ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
        final ResourceLocation lootName = new ResourceLocation(entityId.getNamespace(), "entities/" + entityId.getPath());

        final LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1));
        builder.accept(pool);

        registrar.register(lootName, LootContextParamSets.ALL_PARAMS,
                LootTable.lootTable().withPool(pool));
    }
}
