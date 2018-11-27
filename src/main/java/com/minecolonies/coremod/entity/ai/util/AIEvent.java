package com.minecolonies.coremod.entity.ai.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * One time usage AITarget, unregisters itself after usage
 */
public class AIEvent extends AISpecialTarget
{
    /**
     * Boolean which is checked for unregistering the Event
     */
    private boolean unregister = false;

    /**
     * Event to trigger a one time transition.
     *
     * @param predicate which has to be true to execute
     * @param action    Supplier for the state to transition into
     */
    public AIEvent(@NotNull final BooleanSupplier predicate, @NotNull final Supplier<AIState> action)
    {
        super(AISpecialState.EVENT, false, predicate, action, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param predicate which has to be true to execute
     * @param state     state to transition into
     */
    public AIEvent(@NotNull final BooleanSupplier predicate, @NotNull final AIState state)
    {
        super(AISpecialState.EVENT, false, predicate, () -> state, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param action Supplier for the state to transition into
     */
    public AIEvent(@NotNull final Supplier<AIState> action)
    {
        super(AISpecialState.EVENT, false, () -> true, action, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param state state to transition into
     */
    public AIEvent(@NotNull final AIState state)
    {
        super(AISpecialState.EVENT, false, () -> true, () -> state, 1);
    }

    /**
     * Execute this target.
     * Do some stuff and return the state transition.
     * unregister event if we're transitioning to a state
     *
     * @return the new state the ai is in. null if no change.
     */
    @Override
    public AIState apply()
    {
        // Unregister once a different state is returned.
        final AIState result = super.apply();
        if (result != null)
        {
            unregister = true;
        }
        return result;
    }

    /**
     * Checked to see if we're ready to unregister the event
     *
     * @return false
     */
    @Override
    public boolean shouldUnregister()
    {
        return unregister;
    }
}
