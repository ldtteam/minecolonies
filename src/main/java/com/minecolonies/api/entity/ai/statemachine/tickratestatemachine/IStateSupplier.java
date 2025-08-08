package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import java.io.Serializable;

/**
 * Serializeable version of a supplier for AI states, used for name generation
 *
 * @param <T>
 */
@FunctionalInterface
public interface IStateSupplier<T> extends Serializable
{
    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
