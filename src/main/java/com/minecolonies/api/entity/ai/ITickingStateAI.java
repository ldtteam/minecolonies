package com.minecolonies.api.entity.ai;

import com.minecolonies.api.entity.ai.statemachine.states.IState;

/**
 * Interface for ticking AI's
 */
public interface ITickingStateAI
{
    /**
     * Ticks the ai
     */
    public void tick();

    /**
     * Called when the AI get removed
     */
    public void onRemoval();

    /**
     * Resets the AI as needed
     */
    public void resetAI();

    /**
     * Gets the current state
     *
     * @return
     */
    public IState getState();
}
