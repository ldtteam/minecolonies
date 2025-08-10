package com.minecolonies.api.entity.ai.statemachine.basestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineTransition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Statemachine interface, implement to add more statemachine types. Contains all needed functions for a basic statemachine
 *
 * @param <T> the statemachine transition.
 * @param <S> the State.
 */
public interface IStateMachine<T extends IStateMachineTransition<S>, S extends IState>
{
    /**
     * Adds a transitions to the machine's transition table
     *
     * @param transition the transition to add.
     */
    void addTransition(final T transition);

    /**
     * Removes a transition from the machine's transition table
     *
     * @param transition the transition to remove.
     */
    void removeTransition(final T transition);

    /**
     * Update the statemachine, checks current state and its transitions
     */
    void tick();

    /**
     * Checks the transitions condition
     *
     * @param transition the transition to check.
     * @return true if should run.
     */
    boolean checkTransition(@NotNull final T transition);

    /**
     * Change the state to the next
     *
     * @param transition the transition providing the state change
     * @return true if the transition provided a state to change to, false on null
     */
    boolean transitionToNext(@NotNull final T transition);

    /**
     * Return the current state of the Statemachine
     *
     * @return the state.
     */
    S getState();

    /**
     * Reset the statemachine to the start
     */
    void reset();

    /**
     * Set whether the state history is tracked
     *
     * @param enabled
     * @param memorySize amount of entries kept
     */
    void setHistoryEnabled(boolean enabled, final int memorySize);

    /**
     * Get the state transition history for display
     *
     * @return
     */
    Component getHistory();
}
