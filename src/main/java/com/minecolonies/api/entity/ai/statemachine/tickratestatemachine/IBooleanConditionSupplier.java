package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import java.io.Serializable;

/**
 * Serializeable version of a boolean supplier for AI transitions, used for name generation
 *
 * @param <T>
 */
@FunctionalInterface
public interface IBooleanConditionSupplier extends Serializable
{
    /**
     * Gets a result.
     *
     * @return a result
     */
    boolean getAsBoolean();
}
