package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultSifterCraftingProvider implements DataProvider
{
    private final SifterRecipeProvider recipeProvider;
    private final SifterLootTableProvider lootTableProvider;
    private final Map<Item, List<SifterMeshDetails>> inputs = new HashMap<>();

    public DefaultSifterCraftingProvider(@NotNull final DataGenerator generatorIn,
                                         @NotNull final LootTables lootTableManager)
    {
        inputs.put(Items.DIRT, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(71))
                                .add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(25))
                                .add(LootItem.lootTableItem(Items.OAK_SAPLING).setWeight(1))
                                .add(LootItem.lootTableItem(Items.BIRCH_SAPLING).setWeight(1))
                                .add(LootItem.lootTableItem(Items.SPRUCE_SAPLING).setWeight(1))
                                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(28))
                                .add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(50))
                                .add(LootItem.lootTableItem(Items.OAK_SAPLING).setWeight(5))
                                .add(LootItem.lootTableItem(Items.BIRCH_SAPLING).setWeight(5))
                                .add(LootItem.lootTableItem(Items.SPRUCE_SAPLING).setWeight(5))
                                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING).setWeight(5))
                                .add(LootItem.lootTableItem(Items.CARROT).setWeight(1))
                                .add(LootItem.lootTableItem(Items.POTATO).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly(.025f))
                                .add(EmptyLootItem.emptyItem().setWeight(3))
                                .add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(50))
                                .add(LootItem.lootTableItem(Items.OAK_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.BIRCH_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.SPRUCE_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.CARROT).setWeight(1))
                                .add(LootItem.lootTableItem(Items.POTATO).setWeight(1))
                                .add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(1))
                                .add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(1))
                                .add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(1))
                                .add(LootItem.lootTableItem(Items.DARK_OAK_SAPLING).setWeight(1))
                                .add(LootItem.lootTableItem(Items.ACACIA_SAPLING).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.035f)))
                                .add(EmptyLootItem.emptyItem().setWeight(5))
                                .add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(25))
                                .add(LootItem.lootTableItem(Items.OAK_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.BIRCH_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.SPRUCE_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.CARROT).setWeight(5))
                                .add(LootItem.lootTableItem(Items.POTATO).setWeight(5))
                                .add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(5))
                                .add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(5))
                                .add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(5))
                                .add(LootItem.lootTableItem(Items.DARK_OAK_SAPLING).setWeight(5))
                                .add(LootItem.lootTableItem(Items.ACACIA_SAPLING).setWeight(5))
                        ))

                ));

        inputs.put(Items.GRAVEL, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(85))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(5))
                                .add(LootItem.lootTableItem(Items.FLINT).setWeight(5))
                                .add(LootItem.lootTableItem(Items.COAL).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(60))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(10))
                                .add(LootItem.lootTableItem(Items.FLINT).setWeight(10))
                                .add(LootItem.lootTableItem(Items.COAL).setWeight(10))
                                .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.025f)))
                                .add(EmptyLootItem.emptyItem().setWeight(46))
                                .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(15))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(15))
                                .add(LootItem.lootTableItem(Items.COAL).setWeight(15))
                                .add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(5))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(1))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(1))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
                                .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.035f)))
                                .add(EmptyLootItem.emptyItem().setWeight(40))
                                .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(20))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(20))
                                .add(LootItem.lootTableItem(Items.COAL).setWeight(20))
                                .add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(10))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(2))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(2))
                                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(2))
                                .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(2))
                        ))

                ));

        inputs.put(Items.SAND, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(95))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(2))
                                .add(LootItem.lootTableItem(Items.SUGAR_CANE).setWeight(2))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(85))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(5))
                                .add(LootItem.lootTableItem(Items.SUGAR_CANE).setWeight(5))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.025f)))
                                .add(EmptyLootItem.emptyItem().setWeight(60))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(10))
                                .add(LootItem.lootTableItem(Items.SUGAR_CANE).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(10))
                                .add(LootItem.lootTableItem(Items.COCOA_BEANS).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.035f)))
                                .add(EmptyLootItem.emptyItem().setWeight(40))
                                .add(LootItem.lootTableItem(Items.CACTUS).setWeight(15))
                                .add(LootItem.lootTableItem(Items.SUGAR_CANE).setWeight(15))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(15))
                                .add(LootItem.lootTableItem(Items.COCOA_BEANS).setWeight(15))
                        ))

                ));

        inputs.put(Items.SOUL_SAND, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(90))
                                .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(5))
                                .add(LootItem.lootTableItem(Items.QUARTZ).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(70))
                                .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(10))
                                .add(LootItem.lootTableItem(Items.QUARTZ).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.025f)))
                                .add(EmptyLootItem.emptyItem().setWeight(50))
                                .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(10))
                                .add(LootItem.lootTableItem(Items.QUARTZ).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(10))
                                .add(LootItem.lootTableItem(Items.BLAZE_POWDER).setWeight(1))
                                .add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setBonusRolls(ConstantValue.exactly((0.035f)))
                                .add(EmptyLootItem.emptyItem().setWeight(40))
                                .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(15))
                                .add(LootItem.lootTableItem(Items.QUARTZ).setWeight(15))
                                .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(15))
                                .add(LootItem.lootTableItem(Items.BLAZE_POWDER).setWeight(5))
                                .add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(5))
                                .add(LootItem.lootTableItem(Items.PLAYER_HEAD).setWeight(5))
                        ))

                ));

        recipeProvider = new SifterRecipeProvider(generatorIn, lootTableManager);
        lootTableProvider = new SifterLootTableProvider(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "SifterCraftingProvider";
    }

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        recipeProvider.run(cache);
        lootTableProvider.run(cache);
    }

    private static class SifterMeshDetails
    {
        private final String name;
        private final Item mesh;
        private final int minBuildingLevel;
        private final LootTable.Builder lootTable;

        public SifterMeshDetails(@NotNull final Item mesh, final int minBuildingLevel, @NotNull final LootTable.Builder lootTable)
        {
            this.name = ForgeRegistries.ITEMS.getKey(mesh).getPath().replace("sifter_mesh_", "");
            this.mesh = mesh;
            this.minBuildingLevel = minBuildingLevel;
            this.lootTable = lootTable;
        }

        @NotNull
        public String getName() { return name; }

        @NotNull
        public Item getMesh() { return mesh; }

        public int getMinBuildingLevel() { return minBuildingLevel; }

        @NotNull
        public LootTable.Builder getLootTable() { return lootTable; }
    }

    private class SifterRecipeProvider extends CustomRecipeProvider
    {
        private final LootTables lootTableManager;

        public SifterRecipeProvider(@NotNull final DataGenerator generatorIn,
                                    @NotNull final LootTables lootTableManager)
        {
            super(generatorIn);

            this.lootTableManager = lootTableManager;
        }

        @NotNull
        @Override
        public String getName()
        {
            return "SifterRecipeProvider";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
        {
            for (final Map.Entry<Item, List<SifterMeshDetails>> inputEntry : inputs.entrySet())
            {
                for (final SifterMeshDetails mesh : inputEntry.getValue())
                {
                    final String name = mesh.getName() + "/" + ForgeRegistries.ITEMS.getKey(inputEntry.getKey()).getPath();

                    final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(lootTableManager, mesh.getLootTable().build());
                    final Stream<Item> loot = drops.stream().flatMap(drop -> drop.getItemStacks().stream().map(ItemStack::getItem));

                    CustomRecipeBuilder.create(ModJobs.SIFTER_ID.getPath() + "_custom", name)
                            .inputs(Stream.of(
                                    new ItemStorage(new ItemStack(inputEntry.getKey())),
                                    new ItemStorage(new ItemStack(mesh.getMesh()), true, false))
                                    .collect(Collectors.toList()))
                            .secondaryOutputs(Stream.concat(Stream.of(mesh.getMesh()), loot)
                                        .map(ItemStack::new)
                                        .collect(Collectors.toList()))
                            .lootTable(new ResourceLocation(MOD_ID, "recipes/" + name))
                            .minBuildingLevel(mesh.getMinBuildingLevel())
                            .build(consumer);
                }
            }
        }
    }

    private class SifterLootTableProvider extends SimpleLootTableProvider
    {
        public SifterLootTableProvider(@NotNull final DataGenerator dataGeneratorIn)
        {
            super(dataGeneratorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "SifterLootTableProvider";
        }

        @Override
        protected void registerTables(@NotNull final LootTableRegistrar registrar)
        {
            for (final Map.Entry<Item, List<SifterMeshDetails>> inputEntry : inputs.entrySet())
            {
                for (final SifterMeshDetails mesh : inputEntry.getValue())
                {
                    final String name = mesh.getName() + "/" + ForgeRegistries.ITEMS.getKey(inputEntry.getKey()).getPath();

                    registrar.register(new ResourceLocation(MOD_ID, "recipes/" + name), LootContextParamSets.ALL_PARAMS, mesh.getLootTable());
                }
            }
        }
    }
}
