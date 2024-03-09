package com.minecolonies.core.datalistener;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads and listens to custom visitor data added
 */
public class ColonyStoryDataListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * List of custom visitor data
     */
    public static List<String> abandonedColonyNames = new ArrayList<>();
    public static List<String> abandonedColonyStories = new ArrayList<>();

    /**
     * Create a new listener.
     */
    public ColonyStoryDataListener()
    {
        super(GSON, "stories");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        abandonedColonyNames.clear();
        abandonedColonyStories.clear();

        final JsonObject data = (JsonObject) jsonElementMap.get(new ResourceLocation("minecolonies:abandonedcolonies"));
        final JsonArray namesArray = data.get("names").getAsJsonArray();
        for (final JsonElement name : namesArray)
        {
            abandonedColonyNames.add(name.getAsString());
        }

        final JsonArray storiesJsonArray = data.get("stories").getAsJsonArray();
        for (final JsonElement story : storiesJsonArray)
        {
            abandonedColonyStories.add(story.getAsString());
        }
    }
}
