package com.minecolonies.coremod.generation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
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
public class DatagenLootTableManager extends LootTableManager
{
    private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
    private final ExistingFileHelper existingFileHelper;
    private final Map<ResourceLocation, LootTable> tables = new HashMap<>();

    public DatagenLootTableManager(@NotNull final ExistingFileHelper existingFileHelper)
    {
        super(new LootPredicateManager());  // in theory we should load these too; in practice vanilla doesn't seem to use it

        this.existingFileHelper = existingFileHelper;
    }

    @NotNull
    @Override
    public LootTable get(@NotNull final ResourceLocation location)
    {
        final LootTable table = this.tables.get(location);
        if (table != null) return table;

        try
        {
            final IResource resource = existingFileHelper.getResource(getPreparedPath(location), ResourcePackType.SERVER_DATA);
            try (final InputStream inputstream = resource.getInputStream();
                 final Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            )
            {
                final JsonObject jsonobject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
                final LootTable loottable = ForgeHooks.loadLootTable(GSON, location, jsonobject, false, this);
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
