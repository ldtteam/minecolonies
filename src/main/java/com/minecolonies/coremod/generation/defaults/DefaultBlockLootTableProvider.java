package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
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

    private <T extends Block> void saveBlocks(@NotNull final List<T> blocks, @NotNull final LootTableRegistrar registrar)
    {
        for (final Block block : blocks)
        {
            saveBlock(block, registrar);
        }
    }

    private void saveBlock(@NotNull final Block block, @NotNull final LootTableRegistrar registrar)
    {
        final ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
        if (location != null)
        {
            final ResourceLocation id = new ResourceLocation(location.getNamespace(),
              "blocks/" + location.getPath());

            final LootPoolSingletonContainer.Builder<?> item = LootItem.lootTableItem(block);
            if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
            {
                item.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
            }

            registrar.register(id, LootContextParamSets.BLOCK,
              LootTable.lootTable().withPool(LootPool.lootPool()
                                               .add(item)
                                               .when(ExplosionCondition.survivesExplosion())
              ));
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
