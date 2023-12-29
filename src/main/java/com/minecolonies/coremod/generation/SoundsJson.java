package com.minecolonies.coremod.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SoundsJson implements IJsonSerializable
{
    @NotNull
    private Map<String[], List<String>> sounds = new TreeMap<>();

    public SoundsJson() {}

    public SoundsJson(@NotNull final Map<String[], List<String>> sounds)
    {
        this.sounds = ensureTreeMap(sounds);
    }

    public static <K, V> Map<K, V> ensureTreeMap(final Map<K, V> map)
    {
        return map instanceof TreeMap ? map : new TreeMap<>(map);
    }

    /**
     * Creates a sound json object for the list of names and its properties
     *
     * @param category   category to set
     * @param properties JsonObject properties to use
     * @param names      List of names to set
     * @return new JsonObject
     */
    public static JsonObject createSoundJson(final String category, final JsonObject properties, final List<String> names)
    {
        JsonObject sound = new JsonObject();
        sound.addProperty("category", category);

        final JsonArray containedSoundList = new JsonArray();
        try
        {
            names.sort(null); // stable output
        }
        catch (UnsupportedOperationException e)
        {
            // immutable collections are fine
        }
        for (final String name : names)
        {
            JsonObject sound1 = new JsonObject();
            sound1.addProperty("name", name);

            for (Map.Entry<String, JsonElement> entry : properties.entrySet())
            {
                sound1.add(entry.getKey(), entry.getValue());
            }

            containedSoundList.add(sound1);
        }

        sound.add("sounds", containedSoundList);
        return sound;
    }

    @NotNull
    @Override
    public JsonElement serialize()
    {
        final JsonObject returnValue = new JsonObject();

        for (final Map.Entry<String[], List<String>> entry : sounds.entrySet())
        {
            final JsonObject defaultValue = new JsonObject();
            defaultValue.addProperty("category", entry.getKey()[1]);

            final JsonArray sounds = new JsonArray();
            for (final String value : entry.getValue())
            {
                final JsonObject sound1 = new JsonObject();
                sound1.addProperty("name", value);
                sound1.addProperty("stream", false);
                sounds.add(sound1);
            }

            defaultValue.add("sounds", sounds);
            returnValue.add(entry.getKey()[0], defaultValue);
        }

        return returnValue;
    }

    @Override
    public void deserialize(@NotNull final JsonElement jsonElement)
    {
        final JsonObject soundsJson = jsonElement.getAsJsonObject();

        for (Map.Entry<String, JsonElement> soundEntry : soundsJson.entrySet())
        {
            final String key = soundEntry.getKey();
            final JsonObject entryJson =  soundEntry.getValue().getAsJsonObject();

            final String category = entryJson.get("category").getAsString();

            final List<String> sounds = new ArrayList<>();
            final JsonArray array = entryJson.getAsJsonArray("sounds");
            for (int i = 0; i < array.size(); i++)
            {
                final JsonObject obj = array.get(i).getAsJsonObject();
                sounds.add(obj.get("name").getAsString());
            }

            this.sounds.put(new String[]{key, category}, sounds);
        }
    }
}
