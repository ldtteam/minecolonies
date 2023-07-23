package com.minecolonies.api.entity.ai.statemachine.states;

/**
 * AI States for citizen's state
 */
public enum CitizenAIState implements IState
{
    IDLE(),
    FLEE(),
    EATING(),
    SICK(),
    SLEEP,
    MOURN,
    WORK,
    WORKING,
    INACTIVE();

    CitizenAIState()
    {

    }
}
