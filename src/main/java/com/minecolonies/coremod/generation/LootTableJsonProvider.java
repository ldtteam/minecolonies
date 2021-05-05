package com.minecolonies.coremod.generation;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Basic data generator for loot table JSON files, based around our LootTableJson rather than the vanilla one.
 */
public abstract class LootTableJsonProvider implements IDataProvider
{
    protected final DataGenerator generator;

    protected LootTableJsonProvider(@NotNull final DataGenerator dataGeneratorIn)
    {
        this.generator = dataGeneratorIn;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "LootTableJsonProvider";
    }

    /**
     * Get the loot tables to be generated.
     * @return the loot tables.
     */
    protected abstract Map<ResourceLocation, LootTableJson> getLootTables();

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        final Map<ResourceLocation, LootTableJson> lootTables = getLootTables();

        for (final Map.Entry<ResourceLocation, LootTableJson> entry : lootTables.entrySet())
        {
            final Path savePath = generator.getOutputFolder()
                    .resolve("data")
                    .resolve(entry.getKey().getNamespace())
                    .resolve("loot_tables")
                    .resolve(entry.getKey().getPath() + ".json");
            IDataProvider.save(DataGeneratorConstants.GSON, cache, entry.getValue().serialize(), savePath);
        }
    }
}
