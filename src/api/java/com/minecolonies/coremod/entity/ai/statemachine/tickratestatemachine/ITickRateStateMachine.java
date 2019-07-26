package com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.coremod.entity.ai.statemachine.basestatemachine.IStateMachine;
import org.jetbrains.annotations.NotNull;

public interface ITickRateStateMachine extends IStateMachine<ITickingTransition>
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
    boolean checkTransition(@NotNull ITickingTransition transition);
}
