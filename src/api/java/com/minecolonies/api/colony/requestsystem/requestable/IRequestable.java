package com.minecolonies.api.colony.requestsystem.requestable;

import com.google.common.reflect.TypeToken;

import java.util.Set;

/**
 * Marker interface for requestable objects.
 */
public interface IRequestable
{
    /**
     * Get the super classes associated to this request type.
     * @return the type.
     */
    Set<TypeToken<?>> getSuperClasses();
}
