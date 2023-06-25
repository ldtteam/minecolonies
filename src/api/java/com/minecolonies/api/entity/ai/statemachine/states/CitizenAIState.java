package com.minecolonies.api.entity.ai.statemachine.states;

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
