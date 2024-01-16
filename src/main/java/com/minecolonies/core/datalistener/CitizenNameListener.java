package com.minecolonies.core.datalistener;

import com.google.gson.*;
import com.minecolonies.api.colony.CitizenNameFile;
import com.minecolonies.api.util.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Loads and listens to custom visitor data added
 */
public class CitizenNameListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * List of custom visitor data
     */
    public static Map<String, CitizenNameFile> nameFileMap = new HashMap<>();

    /**
     * Create a new listener.
     */
    public CitizenNameListener()
    {
        super(GSON, "citizennames");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        nameFileMap.clear();
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
        try
        {
            final JsonObject data = (JsonObject) entry.getValue();

            final int parts = data.get("parts").getAsInt();
            final CitizenNameFile.NameOrder nameOrder = CitizenNameFile.NameOrder.valueOf(data.get("order").getAsString());
            final List<String> maleFirstName = new ArrayList<>();
            final List<String> femaleFirstName = new ArrayList<>();
            final List<String> surnames = new ArrayList<>();

            final JsonArray maleNameJsonArray = data.get("male_firstname").getAsJsonArray();
            for (final JsonElement maleName : maleNameJsonArray)
            {
                maleFirstName.add(maleName.getAsString());
            }

            final JsonArray femaleNameJsonArray = data.get("female_firstname").getAsJsonArray();
            for (final JsonElement femaleName : femaleNameJsonArray)
            {
                femaleFirstName.add(femaleName.getAsString());
            }

            final JsonArray surnameJsonArray = data.get("surnames").getAsJsonArray();
            for (final JsonElement surname : surnameJsonArray)
            {
                surnames.add(surname.getAsString());
            }

            nameFileMap.put(entry.getKey().getPath(), new CitizenNameFile(parts, nameOrder, maleFirstName, femaleFirstName, surnames));
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Could not parse visitor for:" + entry.getKey(), e);
        }
    }
}
