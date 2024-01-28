package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineTransition;

public interface ITickingTransition<S extends IState> extends IStateMachineTransition<S>
{
    /**
     * Returns the intended tickRate of the AITarget
     *
     * @return Tickrate
     */
    int getTickRate();

    /**
     * Allow to dynamically change the tickrate
     *
     * @param tickRate rate at which the AITarget should tick
     */
    void setTickRate(int tickRate);

    /**
     * Returns a preset offset to Ticks
     *
     * @return random
     */
    int countdownTicksToUpdate(final int reduction);

    /**
     * Sets the ticks until next update
     *
     * @param ticksToUpdate
     */
    void setTicksToUpdate(int ticksToUpdate);
}
