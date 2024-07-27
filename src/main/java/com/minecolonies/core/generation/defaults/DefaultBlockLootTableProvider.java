package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.blocks.BlockMinecoloniesRack;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootPool.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DefaultBlockLootTableProvider extends BlockLootSubProvider
{
    public DefaultBlockLootTableProvider()
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    public void generate()
    {
        saveBlocks(Arrays.asList(ModBlocks.getHuts()));

        saveBlock(ModBlocks.blockHutWareHouse);
        saveBlock(ModBlocks.blockStash);

        saveBlock(ModBlocks.blockRack);
        saveBlock(ModBlocks.blockWayPoint);
        saveBlock(ModBlocks.blockBarrel);
        saveBlock(ModBlocks.blockScarecrow);
        saveBlock(ModBlocks.blockPlantationField);
        saveBlock(ModBlocks.blockColonyBanner);
        saveBlock(ModBlocks.blockColonyWallBanner);
        saveBlock(ModBlocks.blockIronGate);
        saveBlock(ModBlocks.blockWoodenGate);
        saveBlock(ModBlocks.blockCompostedDirt,
          lootPool -> lootPool.add(AlternativesEntry.alternatives()
                                     .otherwise(LootItem.lootTableItem(ModBlocks.blockCompostedDirt)
                                                  .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                                                                .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, Ints.exactly(1))))))
                                     .otherwise(LootItem.lootTableItem(Blocks.DIRT)
                                                  .when(ExplosionCondition.survivesExplosion()))));

        saveBlock(ModBlocks.farmland, registrar, lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));
        saveBlock(ModBlocks.floodedFarmland, registrar, lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));

        for (Block block : ModBlocks.getCrops())
        {
            final LootItemBlockStatePropertyCondition.Builder cropCondition = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 6));
            saveBlock(block, registrar, lootPool -> lootPool.add(LootItem.lootTableItem(block.asItem()).when(cropCondition).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)).otherwise(LootItem.lootTableItem(block.asItem()))));
        }

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
        final LootPoolSingletonContainer.Builder<?> item = LootItem.lootTableItem(block);
        if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
        {
            item.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
        }

        this.saveBlock(block, lootPool -> lootPool.add(item).when(ExplosionCondition.survivesExplosion()));
    }

    private void saveBlock(@NotNull final Block block, final Consumer<Builder> lootPoolConfigurer)
    {
            final Builder lootPoolbuilder = LootPool.lootPool();
            lootPoolConfigurer.accept(lootPoolbuilder);
            add(block, LootTable.lootTable().withPool(lootPoolbuilder));
    }

    private void saveBannerBlock(@NotNull final Block block)
    {
            add(block,
              LootTable.lootTable().withPool(LootPool.lootPool()
                                               .add(LootItem.lootTableItem(block))
                                               .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                               .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns").copy("id", "BlockEntityTag.id"))
                                               .when(ExplosionCondition.survivesExplosion())
              ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        return Stream.concat(Arrays.stream(ModBlocks.getHuts()), Stream.of(
            ModBlocks.blockHutWareHouse,
            ModBlocks.blockStash,
            //ModBlocks.blockConstructionTape, // no loot table
            ModBlocks.blockRack,
            ModBlocks.blockWayPoint,
            ModBlocks.blockBarrel,
            ModBlocks.blockScarecrow,
            ModBlocks.blockPlantationField,
            ModBlocks.blockColonyBanner,
            ModBlocks.blockColonyWallBanner,
            ModBlocks.blockIronGate,
            ModBlocks.blockWoodenGate,
            ModBlocks.blockCompostedDirt,
            //ModBlocks.blockDecorationPlaceholder, // creative only

            Blocks.BLACK_BANNER,
            Blocks.BLUE_BANNER,
            Blocks.BROWN_BANNER,
            Blocks.WHITE_BANNER,
            Blocks.CYAN_BANNER,
            Blocks.GRAY_BANNER,
            Blocks.GREEN_BANNER,
            Blocks.LIGHT_BLUE_BANNER,
            Blocks.LIGHT_GRAY_BANNER,
            Blocks.LIME_BANNER,
            Blocks.MAGENTA_BANNER,
            Blocks.ORANGE_BANNER,
            Blocks.PINK_BANNER,
            Blocks.PURPLE_BANNER,
            Blocks.RED_BANNER,
            Blocks.YELLOW_BANNER
        )).map(Block.class::cast).toList();
    }
}
