package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounter;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounterManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Loader for json based expedition encounters.
 */
public class ExpeditionEncounterListener extends SimpleJsonResourceReloadListener
{
    /**
     * The gson instance.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: (namespace)/colony/expedition_encounters/(path)
     */
    public ExpeditionEncounterListener()
    {
        super(GSON, "colony/expedition_encounters");
    }

    @Override
    protected void apply(
      @NotNull final Map<ResourceLocation, JsonElement> object,
      @NotNull final ResourceManager resourceManager,
      @NotNull final ProfilerFiller profiler)
    {
        Log.getLogger().info("Beginning load of expedition encounters.");

        final Map<ResourceLocation, ExpeditionEncounter> newTypes = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final ExpeditionEncounter parsed = ExpeditionEncounter.parse(key, entry.getValue().getAsJsonObject());
                newTypes.put(key, parsed);
            }
            catch (final JsonParseException | NullPointerException e)
            {
                Log.getLogger().error(new FormattedMessage("Error parsing expedition encounter {}", new Object[] {key}, e));
            }
        }

        final ExpeditionEncounterManager manager = ExpeditionEncounterManager.getInstance();
        manager.reloadEncounters(newTypes);
    }
}