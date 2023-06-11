package com.minecolonies.coremod.generation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a LootTableManager that's populated on-demand during datagen, so that we
 * can look up other tables for {@link com.minecolonies.coremod.colony.crafting.LootTableAnalyzer}.
 */
public class DatagenLootTableManager extends LootDataManager
{
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
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
        final LootTable table = this.tables.get(location);
        if (table != null) return table;

        try
        {
            final Resource resource = existingFileHelper.getResource(location, PackType.SERVER_DATA);
            try (final InputStream inputstream = resource.open();
                 final Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            )
            {
                final JsonElement jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                final LootTable loottable = ForgeHooks.loadLootTable(GSON, location, jsonobject, false);
                if (loottable != null)
                {
                    this.tables.put(location, loottable);
                    return loottable;
                }
            }
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }

        return LootTable.EMPTY;
    }
}
