package com.minecolonies.api.entity.ai.statemachine.basestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineTransition;
import org.jetbrains.annotations.NotNull;

/**
 * Statemachine interface, implement to add more statemachine types.
 * Contains all needed functions for a basic statemachine
 * @param <T> the statemachine transition.
 * @param <S> the State.
 */
public interface IStateMachine<T extends IStateMachineTransition<S>, S extends IState>
{
    /**
     * Adds a transitions to the machine's transition table
     * @param transition the transition to add.
     */
    void addTransition(final T transition);

    /**
     * Removes a transition from the machine's transition table
     * @param transition the transition to remove.
     */
    void removeTransition(final T transition);

    /**
     * Update the statemachine, checks current state and its transitions
     */
    void tick();

    /**
     * Checks the transitions condition
     * @param transition the transition to check.
     * @return true if should run.
     */
    boolean checkTransition(@NotNull final T transition);

    /**
     * Change the state to the next
     * @param transition the next transition.
     * @return true if transitioned.
     */
    boolean transitionToNext(@NotNull final T transition);

    /**
     * Return the current state of the Statemachine
     * @return the state.
     */
    S getState();

    /**
     * Reset the statemachine to the start
     */
    void reset();
}
