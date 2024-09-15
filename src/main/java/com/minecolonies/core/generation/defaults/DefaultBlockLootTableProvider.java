package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.blocks.BlockMinecoloniesRack;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
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
import net.minecraft.world.level.storage.loot.functions.SetBannerPatternFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DefaultBlockLootTableProvider extends BlockLootSubProvider
{
    public DefaultBlockLootTableProvider(@NotNull final HolderLookup.Provider provider)
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    public void generate()
    {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        saveBlocks(ModBlocks.getHuts());

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
                                                  .when(this.hasSilkTouch()))
                                     .otherwise(LootItem.lootTableItem(Blocks.DIRT)
                                                  .when(ExplosionCondition.survivesExplosion()))));

        saveBlock(ModBlocks.farmland, lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));
        saveBlock(ModBlocks.floodedFarmland, lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));

        for (DeferredBlock<MinecoloniesCropBlock> block : ModBlocks.getCrops())
        {
            final LootItemBlockStatePropertyCondition.Builder cropCondition = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 6));
            saveBlock(block, lootPool -> lootPool.add(LootItem.lootTableItem(block.asItem()).when(cropCondition).apply(ApplyBonusCount.addBonusBinomialDistributionCount(registrylookup.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3)).otherwise(LootItem.lootTableItem(block.asItem()))));
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

    private <T extends Block> void saveBlocks(@NotNull final List<DeferredBlock<? extends T>> blocks)
    {
        for (final DeferredBlock<?> block : blocks)
        {
            saveBlock(block);
        }
    }

    private void saveBlock(@NotNull final DeferredBlock<?> block)
    {
        final LootPoolSingletonContainer.Builder<?> item = LootItem.lootTableItem(block);
        if (block.get() instanceof AbstractBlockHut || block.get() instanceof BlockMinecoloniesRack)
        {
            item.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
        }

        this.saveBlock(block, lootPool -> lootPool.add(item).when(ExplosionCondition.survivesExplosion()));
    }

    private void saveBlock(@NotNull final DeferredBlock<?> block, final Consumer<Builder> lootPoolConfigurer)
    {
            final Builder lootPoolbuilder = LootPool.lootPool();
            lootPoolConfigurer.accept(lootPoolbuilder);
            add(block.get(), LootTable.lootTable().withPool(lootPoolbuilder));
    }

    private void saveBannerBlock(@NotNull final Block block)
    {
            add(block,
              LootTable.lootTable().withPool(LootPool.lootPool()
                                               .add(LootItem.lootTableItem(block))
                                               .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                               .apply(SetBannerPatternFunction.setBannerPattern(false))
                                               .when(ExplosionCondition.survivesExplosion())
              ));
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        final List<DeferredBlock<?>> blocks = new ArrayList<>();
        blocks.addAll(ModBlocks.getCrops());
        blocks.addAll(ModBlocks.getHuts());
        blocks.addAll(List.of(
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
            ModBlocks.floodedFarmland,
            ModBlocks.farmland));

        return Stream.concat(blocks.stream().map(DeferredBlock::get).map(Block.class::cast), Stream.of(
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
        )).toList();
    }
}
