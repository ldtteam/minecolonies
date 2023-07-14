package com.minecolonies.coremod.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.generation.CustomRecipeAndLootTableProvider;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Datagen for Nether Worker
 */
public class DefaultNetherWorkerLootProvider extends CustomRecipeAndLootTableProvider
{
    public static final String NETHERWORKER = ModJobs.NETHERWORKER_ID.getPath();
    private static final int MAX_BUILDING_LEVEL = 5;

    private final LootTables lootTableManager;
    private final List<LootTable.Builder> levels;

    public DefaultNetherWorkerLootProvider(@NotNull final DataGenerator generatorIn,
                                           @NotNull final LootTables lootTableManager)
    {
        super(generatorIn);
        this.lootTableManager = lootTableManager;

        levels = new ArrayList<>();

        for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
        {
            levels.add(createTripLoot(buildingLevel));
        }
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
                .setRolls(UniformGenerator.between(3, 10))
                .setBonusRolls(UniformGenerator.between(0.3F, 0.3F));

        blocks.add(LootItem.lootTableItem(Items.NETHERRACK)
                .setWeight(20)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 25))));

        blocks.add(LootItem.lootTableItem(Items.SOUL_SAND)
                .setWeight(10)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 7))));

        blocks.add(LootItem.lootTableItem(Items.SOUL_SOIL)
                .setWeight(8)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))));

        blocks.add(LootItem.lootTableItem(Items.GRAVEL)
                .setWeight(10)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 10))));

        blocks.add(LootItem.lootTableItem(Items.NETHER_QUARTZ_ORE)
                .setWeight(15)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))));

        if (buildingLevel >= 2)
        {
            blocks.add(LootItem.lootTableItem(Items.GLOWSTONE)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))));

            blocks.add(LootItem.lootTableItem(Items.NETHER_WART)
                    .setWeight(3)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

            blocks.add(LootItem.lootTableItem(Items.BROWN_MUSHROOM)
                    .setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

            blocks.add(LootItem.lootTableItem(Items.RED_MUSHROOM)
                    .setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

            blocks.add(LootItem.lootTableItem(Items.CRIMSON_NYLIUM)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1))));

            blocks.add(LootItem.lootTableItem(Items.CRIMSON_FUNGUS)
                    .setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1))));

            blocks.add(LootItem.lootTableItem(Items.CRIMSON_STEM)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        }

        if (buildingLevel >= 3)
        {
            blocks.add(LootItem.lootTableItem(Items.BASALT)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));

            blocks.add(LootItem.lootTableItem(Items.WARPED_NYLIUM)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1))));

            blocks.add(LootItem.lootTableItem(Items.WARPED_FUNGUS)
                    .setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1))));

            blocks.add(LootItem.lootTableItem(Items.WARPED_STEM)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        }

        if (buildingLevel >= 4)
        {
            blocks.add(LootItem.lootTableItem(Items.NETHER_GOLD_ORE)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

            blocks.add(LootItem.lootTableItem(Items.BLACKSTONE)
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        }

        if (buildingLevel >= 5)
        {
            blocks.add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS)
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));
        }

        return blocks;
    }

    @NotNull
    private LootPool.Builder createMobsPool(final int buildingLevel)
    {
        final LootPool.Builder mobs = new LootPool.Builder()
                .name("mobs")
                .setRolls(UniformGenerator.between(2, 6))
                .setBonusRolls(UniformGenerator.between(0.1F, 0.1F));

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

    private LootPoolSingletonContainer.Builder<?> createAdventureToken(@NotNull final EntityType<?> mob, final int damage_done, final int xp_gained)
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES.getKey(mob).toString());
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
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        final List<ItemStorage> inputs = Arrays.asList(
                new ItemStorage(new ItemStack(Items.COBBLESTONE, 64)),
                new ItemStorage(new ItemStack(Items.TORCH, 32)),
                new ItemStorage(new ItemStack(Items.LADDER, 16))
        );

        for (int i = 0; i < levels.size(); ++i)
        {
            final int buildingLevel = i + 1;

            final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(lootTableManager, levels.get(i).build());
            final Stream<Item> loot = drops.stream().flatMap(drop -> drop.getItemStacks().stream().map(ItemStack::getItem));

            CustomRecipeProvider.CustomRecipeBuilder.create(NETHERWORKER, MODULE_CUSTOM, "trip" + buildingLevel)
                    .minBuildingLevel(buildingLevel)
                    .maxBuildingLevel(buildingLevel)
                    .inputs(inputs)
                    .secondaryOutputs(loot.map(ItemStack::new).collect(Collectors.toList()))
                    .lootTable(new ResourceLocation(MOD_ID, "recipes/" + NETHERWORKER + "/trip" + buildingLevel))
                    .build(consumer);
        }

        // and also a lava bucket recipe for good measure
        CustomRecipeProvider.CustomRecipeBuilder.create(NETHERWORKER, MODULE_CUSTOM, "lava")
                .inputs(Collections.singletonList(new ItemStorage(new ItemStack(Items.BUCKET))))
                .result(new ItemStack(Items.LAVA_BUCKET))
                .build(consumer);

        // and some froglights, because why not
        frogLight(consumer, Items.OCHRE_FROGLIGHT, Items.LILY_PAD);
        frogLight(consumer, Items.PEARLESCENT_FROGLIGHT, Items.MAGMA_BLOCK);
        frogLight(consumer, Items.VERDANT_FROGLIGHT, Items.SNOW_BLOCK);
    }

    private void frogLight(@NotNull final Consumer<FinishedRecipe> consumer,
                           @NotNull final Item frogLight,
                           @NotNull final Item flavour)
    {
        CustomRecipeProvider.CustomRecipeBuilder.create(NETHERWORKER, MODULE_CUSTOM, ForgeRegistries.ITEMS.getKey(frogLight).getPath())
                .inputs(List.of(new ItemStorage(new ItemStack(Items.SLIME_BLOCK)),
                        new ItemStorage(new ItemStack(Items.MAGMA_CREAM)),
                        new ItemStorage(new ItemStack(flavour, 4))))
                .result(new ItemStack(frogLight))
                .minBuildingLevel(3)
                .build(consumer);
    }

    @Override
    protected void registerTables(@NotNull final SimpleLootTableProvider.LootTableRegistrar registrar)
    {
        for (int i = 0; i < levels.size(); ++i)
        {
            final int buildingLevel = i + 1;
            final LootTable.Builder lootTable = levels.get(i);

            registrar.register(new ResourceLocation(MOD_ID, "recipes/" + NETHERWORKER + "/trip" + buildingLevel),
                    LootContextParamSets.ALL_PARAMS, lootTable);
        }
    }
}
