package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DefaultBlockLootTableProvider extends LootTableProvider
{

    public DefaultBlockLootTableProvider(final PackOutput output)
    {
        super(output, Set.of(), List.of(new SubProviderEntry(DefaultBlockLootTableProvider.DefaultBlockLootTableEntries::new, LootContextParamSets.BLOCK)));
    }

    private static final class DefaultBlockLootTableEntries extends BlockLootSubProvider
    {
        private DefaultBlockLootTableEntries() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate()
        {
            saveBlocks(Arrays.asList(ModBlocks.getHuts()));

            saveBlock(ModBlocks.blockHutWareHouse);
            saveBlock(ModBlocks.blockStash);

            saveBlock(ModBlocks.blockConstructionTape);
            saveBlock(ModBlocks.blockRack);
            saveBlock(ModBlocks.blockWayPoint);
            saveBlock(ModBlocks.blockBarrel);
            saveBlock(ModBlocks.blockScarecrow);
            saveBlock(ModBlocks.blockColonyBanner);
            saveBlock(ModBlocks.blockIronGate);
            saveBlock(ModBlocks.blockWoodenGate);

            // intentionally no drops -- creative only
            //saveBlock(ModBlocks.blockDecorationPlaceholder);

            saveBannerBlock(Blocks.BLACK_BANNER);
            saveBannerBlock(Blocks.BLUE_BANNER);
            saveBannerBlock(Blocks.BROWN_BANNER);
            saveBannerBlock(Blocks.WHITE_BANNER);
            saveBannerBlock(Blocks.CYAN_BANNER);
            saveBannerBlock(Blocks.GRAY_BANNER);
            saveBannerBlock(Blocks.GREEN_BANNER);
            saveBannerBlock(Blocks.LIGHT_BLUE_BANNER);
            saveBannerBlock(Blocks.LIGHT_GRAY_BANNER);
            saveBannerBlock(Blocks.LIME_BANNER);
            saveBannerBlock(Blocks.MAGENTA_BANNER);
            saveBannerBlock(Blocks.ORANGE_BANNER);
            saveBannerBlock(Blocks.PINK_BANNER);
            saveBannerBlock(Blocks.PURPLE_BANNER);
            saveBannerBlock(Blocks.RED_BANNER);
            saveBannerBlock(Blocks.YELLOW_BANNER);

            saveBannerBlock(Blocks.BLACK_WALL_BANNER);
            saveBannerBlock(Blocks.BLUE_WALL_BANNER);
            saveBannerBlock(Blocks.BROWN_WALL_BANNER);
            saveBannerBlock(Blocks.WHITE_WALL_BANNER);
            saveBannerBlock(Blocks.CYAN_WALL_BANNER);
            saveBannerBlock(Blocks.GRAY_WALL_BANNER);
            saveBannerBlock(Blocks.GREEN_WALL_BANNER);
            saveBannerBlock(Blocks.LIGHT_BLUE_WALL_BANNER);
            saveBannerBlock(Blocks.LIGHT_GRAY_WALL_BANNER);
            saveBannerBlock(Blocks.LIME_WALL_BANNER);
            saveBannerBlock(Blocks.MAGENTA_WALL_BANNER);
            saveBannerBlock(Blocks.ORANGE_WALL_BANNER);
            saveBannerBlock(Blocks.PINK_WALL_BANNER);
            saveBannerBlock(Blocks.PURPLE_WALL_BANNER);
            saveBannerBlock(Blocks.RED_WALL_BANNER);
            saveBannerBlock(Blocks.YELLOW_WALL_BANNER);
        }

        private <T extends Block> void saveBlocks(@NotNull final List<T> blocks)
        {
            for (final Block block : blocks)
            {
                saveBlock(block);
            }
        }

        private void saveBlock(@NotNull final Block block)
        {
            final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
            if (location != null)
            {
                final ResourceLocation id = new ResourceLocation(location.getNamespace(),
                  "block/" + location.getPath());

                final LootPoolSingletonContainer.Builder<?> item = LootItem.lootTableItem(block);
                if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
                {
                    item.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
                }

               this.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                                                   .add(item)
                                                   .when(ExplosionCondition.survivesExplosion())
                  ));
            }
        }

        private void saveBannerBlock(@NotNull final Block block)
        {
            final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
            if (location != null)
            {
                this.add(block,
                  LootTable.lootTable().withPool(LootPool.lootPool()
                                                   .add(LootItem.lootTableItem(block))
                                                   .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                                   .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                            .copy("Patterns", "BlockEntityTag.Patterns")
                                                            .copy("id", "BlockEntityTag.id"))
                                                   .when(ExplosionCondition.survivesExplosion())
                  ));
            }
        }
    }
}
