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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
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

public class DefaultSifterCraftingProvider implements IDataProvider
{
    private final SifterRecipeProvider recipeProvider;
    private final SifterLootTableProvider lootTableProvider;
    private final Map<Item, List<SifterMeshDetails>> inputs = new HashMap<>();

    public DefaultSifterCraftingProvider(@NotNull final DataGenerator generatorIn)
    {
        inputs.put(Items.DIRT, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(71))
                                .add(ItemLootEntry.lootTableItem(Items.WHEAT_SEEDS).setWeight(25))
                                .add(ItemLootEntry.lootTableItem(Items.OAK_SAPLING).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.BIRCH_SAPLING).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.SPRUCE_SAPLING).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.JUNGLE_SAPLING).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(28))
                                .add(ItemLootEntry.lootTableItem(Items.WHEAT_SEEDS).setWeight(50))
                                .add(ItemLootEntry.lootTableItem(Items.OAK_SAPLING).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.BIRCH_SAPLING).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.SPRUCE_SAPLING).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.JUNGLE_SAPLING).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.CARROT).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.POTATO).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.025f, 0.025f)
                                .add(EmptyLootEntry.emptyItem().setWeight(3))
                                .add(ItemLootEntry.lootTableItem(Items.WHEAT_SEEDS).setWeight(50))
                                .add(ItemLootEntry.lootTableItem(Items.OAK_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.BIRCH_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.SPRUCE_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.JUNGLE_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.CARROT).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.POTATO).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.MELON_SEEDS).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.BEETROOT_SEEDS).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.DARK_OAK_SAPLING).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.ACACIA_SAPLING).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.035f, 0.035f)
                                .add(EmptyLootEntry.emptyItem().setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.WHEAT_SEEDS).setWeight(25))
                                .add(ItemLootEntry.lootTableItem(Items.OAK_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.BIRCH_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.SPRUCE_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.JUNGLE_SAPLING).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.CARROT).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.POTATO).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.MELON_SEEDS).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.BEETROOT_SEEDS).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.DARK_OAK_SAPLING).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.ACACIA_SAPLING).setWeight(5))
                        ))

                ));

        inputs.put(Items.GRAVEL, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(85))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_NUGGET).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.FLINT).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.COAL).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(60))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_NUGGET).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.FLINT).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.COAL).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.REDSTONE).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.025f, 0.025f)
                                .add(EmptyLootEntry.emptyItem().setWeight(46))
                                .add(ItemLootEntry.lootTableItem(Items.REDSTONE).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_NUGGET).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.COAL).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.LAPIS_LAZULI).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_INGOT).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.GOLD_INGOT).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.EMERALD).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.DIAMOND).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.035f, 0.035f)
                                .add(EmptyLootEntry.emptyItem().setWeight(40))
                                .add(ItemLootEntry.lootTableItem(Items.REDSTONE).setWeight(20))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_NUGGET).setWeight(20))
                                .add(ItemLootEntry.lootTableItem(Items.COAL).setWeight(20))
                                .add(ItemLootEntry.lootTableItem(Items.LAPIS_LAZULI).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.IRON_INGOT).setWeight(2))
                                .add(ItemLootEntry.lootTableItem(Items.GOLD_INGOT).setWeight(2))
                                .add(ItemLootEntry.lootTableItem(Items.EMERALD).setWeight(2))
                                .add(ItemLootEntry.lootTableItem(Items.DIAMOND).setWeight(2))
                        ))

                ));

        inputs.put(Items.SAND, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(95))
                                .add(ItemLootEntry.lootTableItem(Items.CACTUS).setWeight(2))
                                .add(ItemLootEntry.lootTableItem(Items.SUGAR_CANE).setWeight(2))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(85))
                                .add(ItemLootEntry.lootTableItem(Items.CACTUS).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.SUGAR_CANE).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.GOLD_NUGGET).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.025f, 0.025f)
                                .add(EmptyLootEntry.emptyItem().setWeight(60))
                                .add(ItemLootEntry.lootTableItem(Items.CACTUS).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.SUGAR_CANE).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.GOLD_NUGGET).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.COCOA_BEANS).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.035f, 0.035f)
                                .add(EmptyLootEntry.emptyItem().setWeight(40))
                                .add(ItemLootEntry.lootTableItem(Items.CACTUS).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.SUGAR_CANE).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.GOLD_NUGGET).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.COCOA_BEANS).setWeight(15))
                        ))

                ));

        inputs.put(Items.SOUL_SAND, Arrays.asList(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(90))
                                .add(ItemLootEntry.lootTableItem(Items.NETHER_WART).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.QUARTZ).setWeight(5))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootEntry.emptyItem().setWeight(70))
                                .add(ItemLootEntry.lootTableItem(Items.NETHER_WART).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.QUARTZ).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.GLOWSTONE_DUST).setWeight(10))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.025f, 0.025f)
                                .add(EmptyLootEntry.emptyItem().setWeight(50))
                                .add(ItemLootEntry.lootTableItem(Items.NETHER_WART).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.QUARTZ).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.GLOWSTONE_DUST).setWeight(10))
                                .add(ItemLootEntry.lootTableItem(Items.BLAZE_POWDER).setWeight(1))
                                .add(ItemLootEntry.lootTableItem(Items.MAGMA_CREAM).setWeight(1))
                        )),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .bonusRolls(0.035f, 0.035f)
                                .add(EmptyLootEntry.emptyItem().setWeight(40))
                                .add(ItemLootEntry.lootTableItem(Items.NETHER_WART).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.QUARTZ).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.GLOWSTONE_DUST).setWeight(15))
                                .add(ItemLootEntry.lootTableItem(Items.BLAZE_POWDER).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.MAGMA_CREAM).setWeight(5))
                                .add(ItemLootEntry.lootTableItem(Items.PLAYER_HEAD).setWeight(5))
                        ))

                ));

        recipeProvider = new SifterRecipeProvider(generatorIn);
        lootTableProvider = new SifterLootTableProvider(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "SifterCraftingProvider";
    }

    @Override
    public void run(@NotNull final DirectoryCache cache) throws IOException
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
            this.name = mesh.getRegistryName().getPath().replace("sifter_mesh_", "");
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
        public SifterRecipeProvider(@NotNull final DataGenerator generatorIn)
        {
            super(generatorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "SifterRecipeProvider";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
        {
            for (final Map.Entry<Item, List<SifterMeshDetails>> inputEntry : inputs.entrySet())
            {
                for (final SifterMeshDetails mesh : inputEntry.getValue())
                {
                    final String name = mesh.getName() + "/" + inputEntry.getKey().getRegistryName().getPath();

                    final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(null, mesh.getLootTable().build());
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
                    final String name = mesh.getName() + "/" + inputEntry.getKey().getRegistryName().getPath();

                    registrar.register(new ResourceLocation(MOD_ID, "recipes/" + name), LootParameterSets.ALL_PARAMS, mesh.getLootTable());
                }
            }
        }
    }
}
