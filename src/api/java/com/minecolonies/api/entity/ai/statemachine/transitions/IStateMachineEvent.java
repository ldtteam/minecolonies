package com.minecolonies.api.entity.ai.statemachine.transitions;

import com.minecolonies.api.entity.ai.statemachine.states.IAIEventType;

/**
 * Event transition type for Statemachines
 */
public interface IStateMachineEvent extends IStateMachineTransition
{
    /**
     * Get the event type of the transition
     *
     * @return event type
     */
    IAIEventType getEventType();
}
