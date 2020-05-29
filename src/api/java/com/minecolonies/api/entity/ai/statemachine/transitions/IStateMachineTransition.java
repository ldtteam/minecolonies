package com.minecolonies.api.entity.ai.statemachine.transitions;

import com.minecolonies.api.entity.ai.statemachine.states.IState;

/**
 * Transition type for Statemachines
 */
public interface IStateMachineTransition<S extends IState>
{
    /**
     * Get the state of the transition
     *
     * @return state
     */
    S getState();

    /**
     * Get the next state the Transition goes into
     * @return the next state to go to.
     */
    S getNextState();

    /**
     * Check if the condition of the transition is fulfilled
     *
     * @return true if ready to transition to next state
     */
    boolean checkCondition();
}
