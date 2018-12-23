package com.minecolonies.coremod.entity.ai.statemachine.basestatemachine;

import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.statemachine.transitions.IStateMachineTransition;
import org.jetbrains.annotations.NotNull;

/**
 * Statemachine interface, implement to add more statemachine types.
 * Contains all needed functions for a basic statemachine
 * @param <T>
 */
public interface IStateMachine<T extends IStateMachineTransition>
{
    /**
     * Adds a transitions to the machine's transition table
     */
    void addTransition(final T transition);

    /**
     * Removes a transition from the machine's transition table
     */
    void removeTransition(final T transition);

    /**
     * Update the statemachine, checks current state and its transitions
     */
    void tick();

    /**
     * Checks the transitions condition
     */
    boolean checkTransition(@NotNull final T transition);

    /**
     * Change the state to the next
     */
    boolean transitionToNext(@NotNull final T transition);

    /**
     * Return the current state of the Statemachine
     */
    IAIState getState();

    /**
     * Reset the statemachine to the start
     */
    void reset();
}
