package com.minecolonies.coremod.entity.ai.statemachine.transitions;

import com.minecolonies.coremod.entity.ai.statemachine.states.IAIEventType;

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
