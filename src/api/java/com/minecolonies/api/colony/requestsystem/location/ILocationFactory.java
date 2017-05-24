package com.minecolonies.api.colony.requestsystem.location;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface used to specify a factory for locations.
 * Restricts the output type of the general factory interface to ILocation
 *
 * @param <T> The type of location.
 * @param <L> The location type.
 */
public interface ILocationFactory<T, L extends ILocation> extends IFactory<T, L>
{

    /**
     * Method to get a new instance of the output given the input and additional context data.
     *
     * @param t       The input to build a new output for.
     * @param context The context of the location.
     * @return The new output instance for a given input.
     *
     * @throws IllegalArgumentException is thrown when the factory cannot produce a new instance out of the given context and input.
     */
    @NotNull
    @Override
    default L getNewInstance(@NotNull T t, @NotNull Object... context) throws IllegalArgumentException
    {
        if (context.length != 0)
        {
            throw new IllegalArgumentException("Unsupported context - Too many parameters. None is needed.!");
        }

        return this.getNewInstance(t);
    }

    /**
     * Method to get a new instance of a location given the input.
     *
     * @param input The input to build a new location for.
     * @return The new output instance for a given input.
     */
    @NotNull
    L getNewInstance(@NotNull T input);
}