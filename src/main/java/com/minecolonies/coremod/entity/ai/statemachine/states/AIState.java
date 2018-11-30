package com.minecolonies.coremod.entity.ai.statemachine.states;

/**
 * Interface type for AIState enums
 * Implement this interface to add new statetypes
 */
public interface AIState
{
    /**
     * Check whether we can interrupt the current state to eat
     *
     * @return true if we can
     */
    boolean isOkayToEat();
}
