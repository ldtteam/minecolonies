package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.util.ResourceLocation;
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

            final StandaloneLootEntry.Builder<?> item = ItemLootEntry.lootTableItem(block);
            if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
            {
                item.apply(CopyName.copyName(CopyName.Source.BLOCK_ENTITY));
            }

            registrar.register(id, LootParameterSets.BLOCK,
                    LootTable.lootTable().withPool(LootPool.lootPool()
                            .add(item)
                            .when(SurvivesExplosion.survivesExplosion())
                    ));
        }
    }
}
