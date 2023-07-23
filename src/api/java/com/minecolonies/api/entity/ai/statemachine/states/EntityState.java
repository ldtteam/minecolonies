package com.minecolonies.api.entity.ai.statemachine.states;

/**
 * States of entity loading/activity
 */
public enum EntityState implements IState
{
    // Initial state, entity is loading/data is missing or not present
    INIT,
    // Entity is loaded and ticking on the server
    ACTIVE_SERVER,
    // Entity is loaded and ticking on the client
    ACTIVE_CLIENT,
    // Entity is inactive
    INACTIVE;
}
