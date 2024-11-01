package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.core.blocks.BlockMinecoloniesRack;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DefaultBlockLootTableProvider extends SimpleLootTableProvider
{

    public DefaultBlockLootTableProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    public String getName()
    {
        return "Mcol Loot";
    }

    @Override
    protected void registerTables(@NotNull final LootTableRegistrar registrar)
    {
        saveBlocks(Arrays.asList(ModBlocks.getHuts()), registrar);

        saveBlock(ModBlocks.blockHutWareHouse, registrar);
        saveBlock(ModBlocks.blockStash, registrar);

        saveBlock(ModBlocks.blockConstructionTape, registrar);
        saveBlock(ModBlocks.blockRack, registrar);
        saveBlock(ModBlocks.blockWayPoint, registrar);
        saveBlock(ModBlocks.blockBarrel, registrar);
        saveBlock(ModBlocks.blockScarecrow, registrar);
        saveBlock(ModBlocks.blockPlantationField, registrar);
        saveBlock(ModBlocks.blockColonyBanner, registrar);
        saveBlock(ModBlocks.blockColonyWallBanner, registrar);
        saveBlock(ModBlocks.blockIronGate, registrar);
        saveBlock(ModBlocks.blockWoodenGate, registrar);
        saveBlock(ModBlocks.blockCompostedDirt, registrar,
          lootPool -> lootPool.add(AlternativesEntry.alternatives()
                                     .otherwise(LootItem.lootTableItem(ModBlocks.blockCompostedDirt)
                                                  .when(ModLootConditions.HAS_SILK_TOUCH))
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

        saveBannerBlock(Blocks.BLACK_BANNER, registrar);
        saveBannerBlock(Blocks.BLUE_BANNER, registrar);
        saveBannerBlock(Blocks.BROWN_BANNER, registrar);
        saveBannerBlock(Blocks.WHITE_BANNER, registrar);
        saveBannerBlock(Blocks.CYAN_BANNER, registrar);
        saveBannerBlock(Blocks.GRAY_BANNER, registrar);
        saveBannerBlock(Blocks.GREEN_BANNER, registrar);
        saveBannerBlock(Blocks.LIGHT_BLUE_BANNER, registrar);
        saveBannerBlock(Blocks.LIGHT_GRAY_BANNER, registrar);
        saveBannerBlock(Blocks.LIME_BANNER, registrar);
        saveBannerBlock(Blocks.MAGENTA_BANNER, registrar);
        saveBannerBlock(Blocks.ORANGE_BANNER, registrar);
        saveBannerBlock(Blocks.PINK_BANNER, registrar);
        saveBannerBlock(Blocks.PURPLE_BANNER, registrar);
        saveBannerBlock(Blocks.RED_BANNER, registrar);
        saveBannerBlock(Blocks.YELLOW_BANNER, registrar);

        saveBannerBlock(Blocks.BLACK_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.BLUE_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.BROWN_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.WHITE_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.CYAN_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.GRAY_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.GREEN_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.LIGHT_BLUE_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.LIGHT_GRAY_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.LIME_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.MAGENTA_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.ORANGE_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.PINK_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.PURPLE_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.RED_WALL_BANNER, registrar);
        saveBannerBlock(Blocks.YELLOW_WALL_BANNER, registrar);
    }

    private <T extends Block> void saveBlocks(@NotNull final List<T> blocks, @NotNull final SimpleLootTableProvider.LootTableRegistrar registrar)
    {
        for (final Block block : blocks)
        {
            saveBlock(block, registrar);
        }
    }

    private void saveBlock(@NotNull final Block block, @NotNull final LootTableRegistrar registrar)
    {
        final LootPoolSingletonContainer.Builder<?> item = LootItem.lootTableItem(block);
        if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
        {
            item.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
        }

        this.saveBlock(block, registrar, lootPool -> lootPool.add(item).when(ExplosionCondition.survivesExplosion()));
    }

    private void saveBlock(@NotNull final Block block, @NotNull final LootTableRegistrar registrar, final Consumer<Builder> lootPoolConfigurer)
    {
        final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
        if (location != null)
        {
            final ResourceLocation id = new ResourceLocation(location.getNamespace(),
              "blocks/" + location.getPath());

            final Builder lootPoolbuilder = LootPool.lootPool();
            lootPoolConfigurer.accept(lootPoolbuilder);
            registrar.register(id, LootContextParamSets.BLOCK, LootTable.lootTable().withPool(lootPoolbuilder));
        }
    }

    private void saveBannerBlock(@NotNull final Block block, @NotNull final LootTableRegistrar registrar)
    {
        final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
        if (location != null)
        {
            registrar.register(new ResourceLocation(location.getNamespace(), "blocks/" + location.getPath()), LootContextParamSets.BLOCK,
              LootTable.lootTable().withPool(LootPool.lootPool()
                                               .add(LootItem.lootTableItem(block))
                                               .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                               .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns").copy("id", "BlockEntityTag.id"))
                                               .when(ExplosionCondition.survivesExplosion())
              ));
        }
    }
}
