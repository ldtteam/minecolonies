package com.minecolonies.coremod.generation;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class SifterCraftingProvider implements IDataProvider
{
    private final SifterRecipeProvider recipeProvider;
    private final SifterLootTableProvider lootTableProvider;
    private final Map<Item, List<SifterMeshDetails>> inputs = new HashMap<>();

    public SifterCraftingProvider(@NotNull final DataGenerator generatorIn)
    {
        inputs.put(Items.DIRT, Stream.of(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, new LootTableBuilder()
                        .empty(71)
                        .item(Items.WHEAT_SEEDS, 25)
                        .item(Items.OAK_SAPLING, 1)
                        .item(Items.BIRCH_SAPLING, 1)
                        .item(Items.SPRUCE_SAPLING, 1)
                        .item(Items.JUNGLE_SAPLING, 1)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, new LootTableBuilder()
                        .empty(28)
                        .item(Items.WHEAT_SEEDS, 50)
                        .item(Items.OAK_SAPLING, 5)
                        .item(Items.BIRCH_SAPLING, 5)
                        .item(Items.SPRUCE_SAPLING, 5)
                        .item(Items.JUNGLE_SAPLING, 5)
                        .item(Items.CARROT, 1)
                        .item(Items.POTATO, 1)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, new LootTableBuilder()
                        .bonusRolls(0.025f)
                        .empty(3)
                        .item(Items.WHEAT_SEEDS, 50)
                        .item(Items.OAK_SAPLING, 10)
                        .item(Items.BIRCH_SAPLING, 10)
                        .item(Items.SPRUCE_SAPLING, 10)
                        .item(Items.JUNGLE_SAPLING, 10)
                        .item(Items.CARROT, 1)
                        .item(Items.POTATO, 1)
                        .item(Items.PUMPKIN_SEEDS, 1)
                        .item(Items.MELON_SEEDS, 1)
                        .item(Items.BEETROOT_SEEDS, 1)
                        .item(Items.DARK_OAK_SAPLING, 1)
                        .item(Items.ACACIA_SAPLING, 1)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, new LootTableBuilder()
                        .bonusRolls(0.035f)
                        .empty(5)
                        .item(Items.WHEAT_SEEDS, 25)
                        .item(Items.OAK_SAPLING, 10)
                        .item(Items.BIRCH_SAPLING, 10)
                        .item(Items.SPRUCE_SAPLING, 10)
                        .item(Items.JUNGLE_SAPLING, 10)
                        .item(Items.CARROT, 5)
                        .item(Items.POTATO, 5)
                        .item(Items.PUMPKIN_SEEDS, 5)
                        .item(Items.MELON_SEEDS, 5)
                        .item(Items.BEETROOT_SEEDS, 5)
                        .item(Items.DARK_OAK_SAPLING, 5)
                        .item(Items.ACACIA_SAPLING, 5)
                        .build())

                ).collect(Collectors.toList()));

        inputs.put(Items.GRAVEL, Stream.of(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, new LootTableBuilder()
                        .empty(85)
                        .item(Items.IRON_NUGGET, 5)
                        .item(Items.FLINT, 5)
                        .item(Items.COAL, 5)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, new LootTableBuilder()
                        .empty(60)
                        .item(Items.IRON_NUGGET, 10)
                        .item(Items.FLINT, 10)
                        .item(Items.COAL, 10)
                        .item(Items.REDSTONE, 10)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, new LootTableBuilder()
                        .bonusRolls(0.025f)
                        .empty(46)
                        .item(Items.REDSTONE, 15)
                        .item(Items.IRON_NUGGET, 15)
                        .item(Items.COAL, 15)
                        .item(Items.LAPIS_LAZULI, 5)
                        .item(Items.IRON_INGOT, 1)
                        .item(Items.GOLD_INGOT, 1)
                        .item(Items.EMERALD, 1)
                        .item(Items.DIAMOND, 1)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, new LootTableBuilder()
                        .bonusRolls(0.035f)
                        .empty(40)
                        .item(Items.REDSTONE, 20)
                        .item(Items.IRON_NUGGET, 20)
                        .item(Items.COAL, 20)
                        .item(Items.LAPIS_LAZULI, 10)
                        .item(Items.IRON_INGOT, 2)
                        .item(Items.GOLD_INGOT, 2)
                        .item(Items.EMERALD, 2)
                        .item(Items.DIAMOND, 2)
                        .build())

                ).collect(Collectors.toList()));

        inputs.put(Items.SAND, Stream.of(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, new LootTableBuilder()
                        .empty(95)
                        .item(Items.CACTUS, 2)
                        .item(Items.SUGAR_CANE, 2)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, new LootTableBuilder()
                        .empty(85)
                        .item(Items.CACTUS, 5)
                        .item(Items.SUGAR_CANE, 5)
                        .item(Items.GOLD_NUGGET, 5)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, new LootTableBuilder()
                        .bonusRolls(0.025f)
                        .empty(60)
                        .item(Items.CACTUS, 10)
                        .item(Items.SUGAR_CANE, 10)
                        .item(Items.GOLD_NUGGET, 10)
                        .item(Items.COCOA_BEANS, 10)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, new LootTableBuilder()
                        .bonusRolls(0.035f)
                        .empty(40)
                        .item(Items.CACTUS, 15)
                        .item(Items.SUGAR_CANE, 15)
                        .item(Items.GOLD_NUGGET, 15)
                        .item(Items.COCOA_BEANS, 15)
                        .build())

                ).collect(Collectors.toList()));

        inputs.put(Items.SOUL_SAND, Stream.of(
                new SifterMeshDetails(ModItems.sifterMeshString, 1, new LootTableBuilder()
                        .empty(90)
                        .item(Items.NETHER_WART, 5)
                        .item(Items.QUARTZ, 5)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshFlint, 3, new LootTableBuilder()
                        .empty(70)
                        .item(Items.NETHER_WART, 10)
                        .item(Items.QUARTZ, 10)
                        .item(Items.GLOWSTONE_DUST, 10)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshIron, 4, new LootTableBuilder()
                        .bonusRolls(0.025f)
                        .empty(50)
                        .item(Items.NETHER_WART, 10)
                        .item(Items.QUARTZ, 10)
                        .item(Items.GLOWSTONE_DUST, 10)
                        .item(Items.BLAZE_POWDER, 1)
                        .item(Items.MAGMA_CREAM, 1)
                        .build()),

                new SifterMeshDetails(ModItems.sifterMeshDiamond, 5, new LootTableBuilder()
                        .bonusRolls(0.035f)
                        .empty(40)
                        .item(Items.NETHER_WART, 15)
                        .item(Items.QUARTZ, 15)
                        .item(Items.GLOWSTONE_DUST, 15)
                        .item(Items.BLAZE_POWDER, 5)
                        .item(Items.MAGMA_CREAM, 5)
                        .item(Items.PLAYER_HEAD, 5)
                        .build())

                ).collect(Collectors.toList()));

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
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        recipeProvider.act(cache);
        lootTableProvider.act(cache);
    }

    private static class SifterMeshDetails
    {
        private final String name;
        private final Item mesh;
        private final int minBuildingLevel;
        private final LootTableJson lootTable;

        public SifterMeshDetails(@NotNull final Item mesh, final int minBuildingLevel, @NotNull final LootTableJson lootTable)
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
        public LootTableJson getLootTable() { return lootTable; }
    }

    private static class LootTableBuilder
    {
        private final List<EntryJson> entries = new ArrayList<>();
        private int rolls = 1;
        private float bonusRolls = 0f;

        @NotNull
        public LootTableBuilder rolls(final int rolls)
        {
            this.rolls = rolls;
            return this;
        }

        @NotNull
        public LootTableBuilder bonusRolls(final float bonusRolls)
        {
            this.bonusRolls = bonusRolls;
            return this;
        }

        @NotNull
        public LootTableBuilder empty(final int weight)
        {
            entries.add(new EntryJson(null, EntryTypeEnum.EMPTY, null, null, false, null, weight, 0));
            return this;
        }

        @NotNull
        public LootTableBuilder item(final Item item, final int weight)
        {
            entries.add(new EntryJson(null, EntryTypeEnum.ITEM, item.getRegistryName().toString(), null, false, null, weight, 0));
            return this;
        }

        @NotNull
        public LootTableJson build()
        {
            return new LootTableJson(LootTableTypeEnum.GENERIC,
                    Collections.singletonList(new PoolJson(null, rolls, bonusRolls, entries)));
        }
    }

    private class SifterRecipeProvider extends CustomRecipeProvider
    {
        public SifterRecipeProvider(@NotNull final DataGenerator generatorIn)
        {
            super(generatorIn);
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
        {
            for (final Map.Entry<Item, List<SifterMeshDetails>> inputEntry : inputs.entrySet())
            {
                for (final SifterMeshDetails mesh : inputEntry.getValue())
                {
                    final String name = mesh.getName() + "/" + inputEntry.getKey().getRegistryName().getPath();
                    final Stream<Item> loot = mesh.getLootTable().getPools().stream()
                            .flatMap(pool -> pool.getEntries().stream())
                            .filter(entry -> entry.getType().equals(EntryTypeEnum.ITEM))
                            .map(entry -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getName())));

                    CustomRecipeBuilder.create(ModJobs.SIFTER_ID.getPath(), name)
                            .inputs(Stream.of(new ItemStack(inputEntry.getKey()), new ItemStack(mesh.getMesh())).collect(Collectors.toList()))
                            .secondaryOutputs(Stream.concat(Stream.of(mesh.getMesh()), loot)
                                        .map(ItemStack::new)
                                        .collect(Collectors.toList()))
                            .lootTable(new ResourceLocation(MOD_ID, "recipes/" + name))
                            .minBuildingLevel(mesh.getMinBuildingLevel())
                            .showJei(true)
                            .build(consumer);
                }
            }
        }
    }

    // there is a LootTableProvider but it's a bit more of a pain to work with
    private class SifterLootTableProvider implements IDataProvider
    {
        private final DataGenerator generator;

        public SifterLootTableProvider(@NotNull final DataGenerator dataGeneratorIn)
        {
            this.generator = dataGeneratorIn;
        }

        @NotNull
        @Override
        public String getName()
        {
            return "SifterLootTableProvider";
        }

        @Override
        public void act(@NotNull final DirectoryCache cache) throws IOException
        {
            for (final Map.Entry<Item, List<SifterMeshDetails>> inputEntry : inputs.entrySet())
            {
                for (final SifterMeshDetails mesh : inputEntry.getValue())
                {
                    final String name = mesh.getName() + "/" + inputEntry.getKey().getRegistryName().getPath();

                    final Path savePath = generator.getOutputFolder()
                            .resolve(DataGeneratorConstants.DATAPACK_DIR + "loot_tables/recipes")
                            .resolve(name + ".json");
                    IDataProvider.save(DataGeneratorConstants.GSON, cache, mesh.getLootTable().serialize(), savePath);
                }
            }
        }
    }
}
