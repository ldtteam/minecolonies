package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.core.blocks.BlockMinecoloniesCrop;
import com.minecolonies.core.blocks.BlockMinecoloniesRack;
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
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

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
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        ModBlocks.HUTS.forEach(block -> saveBlock(block.get()));

        saveBlock(ModBlocks.blockHutWareHouse.get());
        saveBlock(ModBlocks.blockStash.get());

        saveBlock(ModBlocks.blockRack.get());
        saveBlock(ModBlocks.blockWayPoint.get());
        saveBlock(ModBlocks.blockBarrel.get());
        saveBlock(ModBlocks.blockScarecrow.get());
        saveBlock(ModBlocks.blockPlantationField.get());
        saveBlock(ModBlocks.blockColonyBanner.get());
        saveBlock(ModBlocks.blockColonyWallBanner.get());
        saveBlock(ModBlocks.blockIronGate.get());
        saveBlock(ModBlocks.blockWoodGate.get());
        saveBlock(ModBlocks.blockCompostedDirt.get(),
          lootPool -> lootPool.add(AlternativesEntry.alternatives()
                                     .otherwise(LootItem.lootTableItem(ModBlocks.blockCompostedDirt)
                                                  .when(ModLootConditions.hasSilkTouch(enchantments)))
                                     .otherwise(LootItem.lootTableItem(Blocks.DIRT)
                                                  .when(ExplosionCondition.survivesExplosion()))));

        saveBlock(ModBlocks.blockFarmland.get(), lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));
        saveBlock(ModBlocks.blockFloodedFarmland.get(), lootPool -> lootPool.add(AlternativesEntry.alternatives().otherwise(LootItem.lootTableItem(Blocks.DIRT))));
        saveBlock(ModBlocks.blockColonySign.get());

        for (DeferredBlock<BlockMinecoloniesCrop> block : ModBlocks.CROPS)
        {
            final LootItemBlockStatePropertyCondition.Builder cropCondition = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 6));
            saveBlock(block.get(), lootPool -> lootPool.add(LootItem.lootTableItem(block.asItem()).when(cropCondition).apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantments.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3)).otherwise(LootItem.lootTableItem(block.asItem()))));
        }

        // intentionally no drops -- creative only
        //saveBlock(ModBlocks.blockDecorationPlaceholder);
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
                                               .apply(SetBannerPatternFunction.setBannerPattern(false))
                                               .when(ExplosionCondition.survivesExplosion())
              ));
    }

    @Override
    @NotNull
    protected Iterable<Block> getKnownBlocks()
    {
        return Stream.concat(ModBlocks.CROPS.stream().map(DeferredHolder::get),
            Stream.concat(ModBlocks.HUTS.stream().map(DeferredHolder::get), Stream.of(ModBlocks.blockStash.get(),
                //ModBlocks.blockConstructionTape, // no loot table
                ModBlocks.blockRack.get(),
                ModBlocks.blockWayPoint.get(),
                ModBlocks.blockBarrel.get(),
                ModBlocks.blockScarecrow.get(),
                ModBlocks.blockPlantationField.get(),
                ModBlocks.blockColonyBanner.get(),
                ModBlocks.blockColonyWallBanner.get(),
                ModBlocks.blockIronGate.get(),
                ModBlocks.blockWoodGate.get(),
                ModBlocks.blockCompostedDirt.get(),
                //ModBlocks.blockDecorationPlaceholder, // creative only
                ModBlocks.blockFloodedFarmland.get(),
                ModBlocks.blockFarmland.get(),
                ModBlocks.blockColonySign.get()))).toList();
    }
}
