package com.minecolonies.core.generation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a LootTableManager that's populated on-demand during datagen, so that we
 * can look up other tables for {@link com.minecolonies.core.colony.crafting.LootTableAnalyzer}.
 */
public class DatagenLootTableManager extends LootDataManager
{
    private static final Gson GSON = new GsonBuilder().create();
    private final ExistingFileHelper               existingFileHelper;
    private final Map<ResourceLocation, LootTable> tables = new HashMap<>();

    public DatagenLootTableManager(@NotNull final ExistingFileHelper existingFileHelper)
    {
        super();  // in theory we should load these too; in practice vanilla doesn't seem to use it
        this.existingFileHelper = existingFileHelper;
    }

    @NotNull
    @Override
    public LootTable getLootTable(@NotNull final ResourceLocation location)
    {
        return tables.computeIfAbsent(location, loc -> {
            LootTable result = null;
            try
            {
                result = loadLootType(loc, LootDataType.TABLE);
            }
            catch (final Throwable e)
            {
                e.printStackTrace();
            }
            return result != null ? result : LootTable.EMPTY;
        });
    }

    @Nullable
    private <T> T loadLootType(final ResourceLocation location, final LootDataType<T> type) throws Exception
    {
        final Resource resource = existingFileHelper.getResource(location, PackType.SERVER_DATA, ".json", type.directory());
        try (final var reader = resource.openAsReader())
        {
            final JsonElement jsonElement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
            return type.deserialize(location, jsonElement).orElse(null);
        }
    }
}
