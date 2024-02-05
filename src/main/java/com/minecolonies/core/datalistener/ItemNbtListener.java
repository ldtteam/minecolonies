package com.minecolonies.core.datalistener;

import com.google.gson.*;
import com.minecolonies.api.items.CheckedNbtKey;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.generation.ItemNbtCalculator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Loads and listens to get custom nbt matching rules.
 */
public class ItemNbtListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Create a new listener.
     */
    public ItemNbtListener()
    {
        super(GSON, "compatibility");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        ItemStackUtils.CHECKED_NBT_KEYS.clear();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            tryParse(entry);
        }
    }

    /**
     * Tries to parse the entry
     *
     * @param entry
     */
    private void tryParse(final Map.Entry<ResourceLocation, JsonElement> entry)
    {
        for (final JsonElement element : entry.getValue().getAsJsonArray())
        {
            try
            {
                final JsonObject jsonObj = element.getAsJsonObject();
                final ResourceLocation itemLoc = new ResourceLocation(jsonObj.get("item").getAsString());
                if (jsonObj.has("checkednbtkeys"))
                {
                    final HashSet<CheckedNbtKey> set = new HashSet<>();
                    final JsonArray jsonArray = jsonObj.getAsJsonArray("checkednbtkeys");
                    for (final JsonElement subElement : jsonArray)
                    {
                        set.add(ItemNbtCalculator.deserializeKeyFromJson(subElement.getAsJsonObject()));
                    }

                    ItemStackUtils.CHECKED_NBT_KEYS.put(ForgeRegistries.ITEMS.getValue(itemLoc), set);
                }
                else
                {
                    ItemStackUtils.CHECKED_NBT_KEYS.put(ForgeRegistries.ITEMS.getValue(itemLoc), new HashSet<>());
                }
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Could not nbt comparator for:" + entry.getKey(), e);
            }
        }
        Log.getLogger().warn("Read " + ItemStackUtils.CHECKED_NBT_KEYS.size() + " items with their nbt keys for compatibility.");
    }
}
