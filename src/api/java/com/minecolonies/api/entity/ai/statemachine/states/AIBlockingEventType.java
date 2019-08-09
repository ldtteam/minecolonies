package com.minecolonies.api.entity.ai.statemachine.states;

/**
 * Event types used in statemachine events.
 */
public enum AIBlockingEventType implements IAIEventType
{
    /*###Priority NonStates###*/
    /**
     * Highest priority state for AITargets, if returning true it stops further execution of the AI this tick.
     * Checked regardless of current state
     */
    AI_BLOCKING,
    /**
     * High priority state which is checked right before trying to execute a normal state AITarget, if returning true it blocks further executions.
     * Checked regardless of current state
     */
    STATE_BLOCKING,
    /**
     * Higher priority state used to do one action and then self consume after returning a State.
     * Checked right after AI_BLOCKING_PRIO targets, return true if blocking further executions of AITargets
     * Checked regardless of current state
     */
    EVENT
}
