package com.minecolonies.core.compatibility.journeymap;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.minecolonies.api.util.Log;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.display.Displayable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Journeymap interface singleton
 */
public class Journeymap
{
    private static Journeymap INSTANCE;

    private IClientAPI        jmap;
    private JourneymapOptions options;

    public Journeymap(final IClientAPI jmap)
    {
        this.jmap = jmap;

        INSTANCE = this;
    }

    /**
     * Gets the current instance.
     */
    @NotNull
    public static Optional<Journeymap> getInstance()
    {
        return Optional.ofNullable(INSTANCE);
    }

    /**
     * Gets the Journeymap API instance.
     */
    @NotNull
    public IClientAPI getApi()
    {
        return this.jmap;
    }

    /**
     * Gets the Journeymap custom options.
     */
    @NotNull
    public Optional<JourneymapOptions> getOptions()
    {
        return Optional.ofNullable(this.options);
    }

    /**
     * Sets the Journeymap custom options.
     * @param options The new options instance.
     */
    public void setOptions(final JourneymapOptions options)
    {
        this.options = options;
    }

    /**
     * Helper method to simplify showing displayables.
     *
     * @param displayable The displayable to show.
     */
    public void show(@NotNull final Displayable displayable)
    {
        try
        {
            getApi().show(displayable);
        }
        catch (final Throwable t)
        {
            // this is already logged by JourneyMap but the API still wants us to catch
        }
    }

    /**
     * Gets the folder where additional map data should be stored.
     *
     * @param dimension The dimension being mapped.
     * @return The path to the data folder (this may not exist yet).
     */
    public Path getDataPath(final ResourceKey<Level> dimension)
    {
        final String name = dimension.location().getPath();
        return this.jmap.getDataPath(MOD_ID).toPath().resolve(name);
    }

    /**
     * Loads JSON data from disk via a Codec.
     *
     * @param filePath The path to the json file (need not exist)
     * @param description What you're trying to load (for error logging).
     * @param codec The codec for the object being loaded.
     * @param <T> The type of the object being loaded.
     * @return The loaded data, or empty if the file was absent or unloadable.
     */
    public <T> Optional<T> loadData(@NotNull final Path filePath,
                                    @NotNull final String description,
                                    @NotNull final Codec<T> codec)
    {
        if (Files.exists(filePath))
        {
            try
            {
                JsonElement json;
                try (final JsonReader reader = new JsonReader(Files.newBufferedReader(filePath)))
                {
                    json = Streams.parse(reader);
                }

                return codec.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> Log.getLogger().error("Failed to load " + description + " from " + filePath));
            }
            catch (final Exception ex)
            {
                Log.getLogger().error("Failed to read " + description + " from " + filePath, ex);
            }
        }

        return Optional.empty();
    }

    /**
     * Saves JSON data to disk via a Codec.
     *
     * @param filePath The path to the json file (need not exist)
     * @param description What you're trying to save (for error logging).
     * @param codec The codec for the object being saved.
     * @param value The object being saved.
     * @param <T> The type of the object being saved.
     * @return True if the object was saved successfully.
     */
    public <T> boolean saveData(@NotNull final Path filePath,
                                @NotNull final String description,
                                @NotNull final Codec<T> codec,
                                @NotNull final T value)
    {
        try
        {
            final JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value)
                    .resultOrPartial(error -> Log.getLogger().error("Failed to save " + description + ": " + error))
                    .orElse(null);
            if (json != null)
            {
                Files.createDirectories(filePath.getParent());
                try (final JsonWriter writer = new JsonWriter(Files.newBufferedWriter(filePath)))
                {
                    //writer.setIndent("  ");
                    Streams.write(json, writer);
                    return true;
                }
            }
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Failed to write " + description + " to " + filePath, ex);
        }
        return false;
    }
}
