package com.minecolonies.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Custom Gson helper class extending {@link net.minecraft.util.GsonHelper} with additional methods.
 */
public class GsonHelper extends net.minecraft.util.GsonHelper
{
    public static <T> String getAsString(final JsonObject object, final String key, final Function<T, String> defaultValue, final T arg)
    {
        return getAsString(object, key, () -> defaultValue.apply(arg));
    }

    public static String getAsString(final JsonObject object, final String key, final Supplier<String> defaultValue)
    {
        return object.has(key) ? convertToString(object.get(key), key) : defaultValue.get();
    }

    public static ResourceLocation getAsResourceLocation(final JsonObject object, final String key)
    {
        return new ResourceLocation(getAsString(object, key));
    }

    public static ResourceLocation getAsResourceLocation(final JsonObject object, final String key, final ResourceLocation defaultValue)
    {
        return Optional.ofNullable(getAsString(object, key, (String) null)).map(ResourceLocation::new).orElse(defaultValue);
    }

    public static <T> JsonArray getAsJsonArray(final JsonObject object, final String key, final Function<T, JsonArray> defaultValue, final T arg)
    {
        return getAsJsonArray(object, key, () -> defaultValue.apply(arg));
    }

    public static JsonArray getAsJsonArray(final JsonObject object, final String key, final Supplier<JsonArray> defaultValue)
    {
        return object.has(key) ? convertToJsonArray(object.get(key), key) : defaultValue.get();
    }
}
