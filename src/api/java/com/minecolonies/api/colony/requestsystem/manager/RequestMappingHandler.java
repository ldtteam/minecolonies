package com.minecolonies.api.colony.requestsystem.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to manage {@link com.minecolonies.api.colony.requestsystem.requestable.IRequestable} to {@link com.minecolonies.api.colony.requestsystem.request.IRequest} mappings.
 */
public final class RequestMappingHandler
{
    /**
     * Holds a map from requestable to its corresponding request type.
     */
    private static final BiMap<Class<?>, Class<?>> requestableMappings = HashBiMap.create();

    public static void registerRequestableTypeMapping(@NotNull final Class<?> requestableType, @NotNull final Class<?> requestType)
    {
        requestableMappings.put(requestableType, requestType);
    }

    /**
     * Method used to get a map with the mappings.
     *
     * @return The mappings.
     */
    public static BiMap<Class<?>, Class<?>> getRequestableMappings()
    {
        return ImmutableBiMap.copyOf(requestableMappings);
    }
}
