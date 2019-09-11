package com.minecolonies.api.entity.ai.statemachine.transitions;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;

/**
 * Transition type for Statemachines
 */
public interface IStateMachineTransition
{
    /**
     * Get the state of the transition
     *
     * @return state
     */
    IAIState getState();

    /**
     * Get the next state the Transition goes into
     */
    IAIState getNextState();

    /**
     * Check if the condition of the transition is fulfilled
     *
     * @return true if ready to transition to next state
     */
    boolean checkCondition();
}
