package com.minecolonies.api.colony.requestsystem.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * A Key-Value-MultiMap Store that handles assignments from a Value to a Key. Allows multiple values to be assigned to a Key.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface IAssignmentDataStore<K, V> extends IDataStore
{
    /**
     * The K to V assignments.
     *
     * @return The assignments
     */
    @NotNull
    Map<K, Collection<V>> getAssignments();

    /**
     * Returns the key for a value by finding the first key it is assigned to.
     *
     * @param value The value to look for.
     * @return The key or null.
     */
    @Nullable
    default K getAssignmentForValue(final V value)
    {
        return getAssignments().keySet().stream().filter(k -> getAssignments().get(k).contains(value)).findFirst().orElse(null);
    }
}
