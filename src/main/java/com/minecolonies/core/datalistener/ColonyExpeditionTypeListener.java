package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Loader for json based expedition types.
 */
public class ColonyExpeditionTypeListener extends SimpleJsonResourceReloadListener
{
    /**
     * The gson instance.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: (namespace)/colony/expedition_types/(path)
     */
    public ColonyExpeditionTypeListener()
    {
        super(GSON, "colony/expedition_types");
    }

    @Override
    protected void apply(
      @NotNull final Map<ResourceLocation, JsonElement> object,
      @NotNull final ResourceManager resourceManager,
      @NotNull final ProfilerFiller profiler)
    {
        Log.getLogger().info("Beginning load of expedition types for colony.");

        final Map<ResourceLocation, ColonyExpeditionType> newTypes = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final ColonyExpeditionType parsed = ColonyExpeditionTypeParser.parse(key, entry.getValue().getAsJsonObject());
                newTypes.put(key, parsed);
            }
            catch (final JsonParseException | NullPointerException e)
            {
                Log.getLogger().error(new FormattedMessage("Error parsing expedition type {}", new Object[] {key}, e));
            }
        }

        final ColonyExpeditionTypeManager manager = ColonyExpeditionTypeManager.getInstance();
        manager.reloadTypes(newTypes);
    }
}