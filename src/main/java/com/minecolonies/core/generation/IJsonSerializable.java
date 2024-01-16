package com.minecolonies.core.generation;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

public interface IJsonSerializable
{
    @NotNull
    JsonElement serialize();

    void deserialize(@NotNull final JsonElement jsonElement);
}
