package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class DefaultNetherWorkerLootProvider implements IDataProvider
{
    private static final int MAX_BUILDING_LEVEL = 5;

    private final NetherWorkerRecipeProvider recipeProvider;
    private final NetherWorkerLootTableProvider lootTableProvider;
    private final List<LootTable.Builder> levels;

    public DefaultNetherWorkerLootProvider(@NotNull final DataGenerator generatorIn)
    {
        levels = new ArrayList<>();

        for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
        {
            levels.add(createTripLoot(buildingLevel));
        }

        recipeProvider = new NetherWorkerRecipeProvider(generatorIn);
        lootTableProvider = new NetherWorkerLootTableProvider(generatorIn);
    }

    private LootTable.Builder createTripLoot(final int buildingLevel)
    {
        return new LootTable.Builder()
                .withPool(createBlocksPool(buildingLevel))
                .withPool(createMobsPool(buildingLevel));
    }

    @NotNull
    private LootPool.Builder createBlocksPool(final int buildingLevel)
    {
        final LootPool.Builder blocks = new LootPool.Builder()
                .name("blocks")
                .setRolls(RandomValueRange.between(3, 10))
                .bonusRolls(0.3F, 0.3F);

        blocks.add(ItemLootEntry.lootTableItem(Items.NETHERRACK)
                .setWeight(20)
                .apply(SetCount.setCount(RandomValueRange.between(5, 25))));

        blocks.add(ItemLootEntry.lootTableItem(Items.SOUL_SAND)
                .setWeight(10)
                .apply(SetCount.setCount(RandomValueRange.between(1, 7))));

        blocks.add(ItemLootEntry.lootTableItem(Items.SOUL_SOIL)
                .setWeight(8)
                .apply(SetCount.setCount(RandomValueRange.between(1, 5))));

        blocks.add(ItemLootEntry.lootTableItem(Items.GRAVEL)
                .setWeight(10)
                .apply(SetCount.setCount(RandomValueRange.between(1, 10))));

        blocks.add(ItemLootEntry.lootTableItem(Items.NETHER_QUARTZ_ORE)
                .setWeight(15)
                .apply(SetCount.setCount(RandomValueRange.between(1, 4))));

        if (buildingLevel >= 2)
        {
            blocks.add(ItemLootEntry.lootTableItem(Items.GLOWSTONE)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(2, 4))));

            blocks.add(ItemLootEntry.lootTableItem(Items.NETHER_WART)
                    .setWeight(3)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 2))));

            blocks.add(ItemLootEntry.lootTableItem(Items.BROWN_MUSHROOM)
                    .setWeight(10)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 2))));

            blocks.add(ItemLootEntry.lootTableItem(Items.RED_MUSHROOM)
                    .setWeight(10)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 2))));

            blocks.add(ItemLootEntry.lootTableItem(Items.CRIMSON_NYLIUM)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 1))));

            blocks.add(ItemLootEntry.lootTableItem(Items.CRIMSON_FUNGUS)
                    .setWeight(10)
                    .apply(SetCount.setCount(RandomValueRange.between(0, 1))));

            blocks.add(ItemLootEntry.lootTableItem(Items.CRIMSON_STEM)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 3))));
        }

        if (buildingLevel >= 3)
        {
            blocks.add(ItemLootEntry.lootTableItem(Items.BASALT)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 3))));

            blocks.add(ItemLootEntry.lootTableItem(Items.WARPED_NYLIUM)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 1))));

            blocks.add(ItemLootEntry.lootTableItem(Items.WARPED_FUNGUS)
                    .setWeight(10)
                    .apply(SetCount.setCount(RandomValueRange.between(0, 1))));

            blocks.add(ItemLootEntry.lootTableItem(Items.WARPED_STEM)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 3))));
        }

        if (buildingLevel >= 4)
        {
            blocks.add(ItemLootEntry.lootTableItem(Items.NETHER_GOLD_ORE)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 2))));

            blocks.add(ItemLootEntry.lootTableItem(Items.BLACKSTONE)
                    .setWeight(5)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 3))));
        }

        if (buildingLevel >= 5)
        {
            blocks.add(ItemLootEntry.lootTableItem(Items.ANCIENT_DEBRIS)
                    .setWeight(1)
                    .apply(SetCount.setCount(RandomValueRange.between(1, 2))));
        }

        return blocks;
    }

    @NotNull
    private LootPool.Builder createMobsPool(final int buildingLevel)
    {
        final LootPool.Builder mobs = new LootPool.Builder()
                .name("mobs")
                .setRolls(RandomValueRange.between(2, 6))
                .bonusRolls(0.1F, 0.1F);

        mobs.add(createAdventureToken(EntityType.ZOMBIFIED_PIGLIN, 5, 5)
                .setWeight(5500).setQuality(-10));

        mobs.add(createAdventureToken(EntityType.MAGMA_CUBE, 3, 4)
                .setWeight(300).setQuality(10));

        mobs.add(createAdventureToken(EntityType.HOGLIN, 3, 5)
                .setWeight(500).setQuality(-1));

        mobs.add(createAdventureToken(EntityType.GHAST, 12, 5)
                .setWeight(300).setQuality(-3));

        mobs.add(createAdventureToken(EntityType.ENDERMAN, 7, 5)
                .setWeight(300).setQuality(-3));

        mobs.add(createAdventureToken(EntityType.BLAZE, 5, 10)
                .setWeight(100).setQuality(1));

        return mobs;
    }

    @NotNull
    private StandaloneLootEntry.Builder<?> createAdventureToken(
            @NotNull final EntityType<?> mob, final int damage_done, final int xp_gained)
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString(TAG_ENTITY_TYPE, mob.getRegistryName().toString());
        nbt.putInt(TAG_DAMAGE, damage_done);
        nbt.putInt(TAG_XP_DROPPED, xp_gained);

        final ItemStack stack = new ItemStack(ModItems.adventureToken);
        stack.setTag(nbt);

        return SimpleLootTableProvider.itemStack(stack);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "NetherWorkerLootProvider";
    }

    @Override
    public void run(@NotNull final DirectoryCache cache) throws IOException
    {
        recipeProvider.run(cache);
        lootTableProvider.run(cache);
    }

    private class NetherWorkerRecipeProvider extends CustomRecipeProvider
    {
        public NetherWorkerRecipeProvider(@NotNull final DataGenerator generatorIn)
        {
            super(generatorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "NetherWorkerRecipeProvider";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
        {
            final List<ItemStorage> inputs = Arrays.asList(
                    new ItemStorage(new ItemStack(Items.COBBLESTONE, 64)),
                    new ItemStorage(new ItemStack(Items.TORCH, 32)),
                    new ItemStorage(new ItemStack(Items.LADDER, 16))
            );

            for (int i = 0; i < levels.size(); ++i)
            {
                final int buildingLevel = i + 1;

                final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(null, levels.get(i).build());
                final Stream<Item> loot = drops.stream().flatMap(drop -> drop.getItemStacks().stream().map(ItemStack::getItem))
                        .filter(item -> !item.equals(ModItems.adventureToken));

                CustomRecipeBuilder.create(ModJobs.NETHERWORKER_ID.getPath() + "_custom", "trip" + buildingLevel)
                        .minBuildingLevel(buildingLevel)
                        .maxBuildingLevel(buildingLevel)
                        .inputs(inputs)
                        .secondaryOutputs(loot.map(ItemStack::new).collect(Collectors.toList()))
                        .lootTable(new ResourceLocation(MOD_ID, "recipes/" + ModJobs.NETHERWORKER_ID.getPath() + "/trip" + buildingLevel))
                        .build(consumer);
            }

            // and also a lava bucket recipe for good measure
            CustomRecipeBuilder.create(ModJobs.NETHERWORKER_ID.getPath() + "_custom", "lava")
                    .inputs(Collections.singletonList(new ItemStorage(new ItemStack(Items.BUCKET))))
                    .result(new ItemStack(Items.LAVA_BUCKET))
                    .build(consumer);
        }
    }

    private class NetherWorkerLootTableProvider extends SimpleLootTableProvider
    {
        public NetherWorkerLootTableProvider(@NotNull final DataGenerator dataGeneratorIn)
        {
            super(dataGeneratorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "NetherWorkerLootTableProvider";
        }

        @Override
        protected void registerTables(@NotNull final LootTableRegistrar registrar)
        {
            for (int i = 0; i < levels.size(); ++i)
            {
                final int buildingLevel = i + 1;
                final LootTable.Builder lootTable = levels.get(i);

                registrar.register(new ResourceLocation(MOD_ID, "recipes/" + ModJobs.NETHERWORKER_ID.getPath() + "/trip" + buildingLevel),
                        LootParameterSets.ALL_PARAMS, lootTable);
            }
        }
    }
}
