package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.basestatemachine.IStateMachine;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import org.jetbrains.annotations.NotNull;

public interface ITickRateStateMachine<S extends IState> extends IStateMachine<ITickingTransition<S>, S>
{
    /**
     * Tick the statemachine.
     */
    @Override
    void tick();

    /**
     * Check the condition for a transition
     *
     * @param transition the target to check
     * @return true if this target worked and we should stop executing this tick
     */
    @Override
    boolean checkTransition(@NotNull ITickingTransition<S> transition);

    /**
     * Returns the current rate the statemachine is beeing ticked at.
     *
     * @return the tickrate
     */
    int getTickRate();

    /**
     * Returns the current rate the statemachine is beeing ticked at.
     * @param tickRate the tick rate to set.
     */
    void setTickRate(final int tickRate);
}
