package com.minecolonies.coremod.generation.defaults;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.conditions.survives_explosion.SurvivesExplosionConditionJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.entry.functions.copy_name.CopyNameFunctionJson;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.generation.DataGeneratorConstants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultBlockLootTableProvider implements IDataProvider
{

    private final DataGenerator generator;

    public DefaultBlockLootTableProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        saveBlocks(Arrays.asList(ModBlocks.getHuts()), cache);

        saveBlock(ModBlocks.blockHutWareHouse, cache);
        saveBlock(ModBlocks.blockPostBox, cache);
        saveBlock(ModBlocks.blockStash, cache);

        saveBlock(ModBlocks.blockConstructionTape, cache);
        saveBlock(ModBlocks.blockRack, cache);
        saveBlock(ModBlocks.blockWayPoint, cache);
        saveBlock(ModBlocks.blockBarrel, cache);
        saveBlock(ModBlocks.blockDecorationPlaceholder, cache);
        saveBlock(ModBlocks.blockScarecrow, cache);
        saveBlock(ModBlocks.blockBarracksTowerSubstitution, cache);
    }

    private <T extends Block> void saveBlocks(final List<T> blocks, final DirectoryCache cache) throws IOException
    {
        for (Block block : blocks)
        {
            saveBlock(block, cache);
        }
    }

    private void saveBlock(final Block block, final DirectoryCache cache) throws IOException
    {
        if (block.getRegistryName() != null)
        {

            final EntryJson entryJson = new EntryJson();
            entryJson.setType(EntryTypeEnum.ITEM);
            entryJson.setName(block.getRegistryName().toString());
            if (block instanceof AbstractBlockHut || block instanceof BlockMinecoloniesRack)
            {
                entryJson.setFunctions(Collections.singletonList(new CopyNameFunctionJson()));
            }

            final PoolJson poolJson = new PoolJson();
            poolJson.setEntries(Collections.singletonList(entryJson));
            poolJson.setRolls(1);
            poolJson.setConditions(Collections.singletonList(new SurvivesExplosionConditionJson()));

            final LootTableJson lootTableJson = new LootTableJson();
            lootTableJson.setType(LootTableTypeEnum.BLOCK);
            lootTableJson.setPools(Collections.singletonList(poolJson));

            final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.LOOT_TABLES_DIR).resolve(block.getRegistryName().getPath() + ".json");
            IDataProvider.save(DataGeneratorConstants.GSON, cache, lootTableJson.serialize(), savePath);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Block Loot Table Provider";
    }
}
