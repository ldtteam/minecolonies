package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class DefaultBlockLootTableProvider extends SimpleLootTableProvider
{
    public DefaultBlockLootTableProvider(@NotNull DataGenerator dataGenerator)
    {
        super(dataGenerator);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Block Loot Table Provider";
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
        saveBlock(ModBlocks.blockDecorationPlaceholder, registrar);
        saveBlock(ModBlocks.blockScarecrow, registrar);
        saveBlock(ModBlocks.blockColonyBanner, registrar);
        saveBlock(ModBlocks.blockIronGate, registrar);
        saveBlock(ModBlocks.blockWoodenGate, registrar);
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
        if (block.getRegistryName() != null)
        {
            final ResourceLocation id = new ResourceLocation(block.getRegistryName().getNamespace(),
                    "blocks/" + block.getRegistryName().getPath());

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
}
