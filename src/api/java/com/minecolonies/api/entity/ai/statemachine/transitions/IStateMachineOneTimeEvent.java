package com.minecolonies.api.entity.ai.statemachine.transitions;

import com.minecolonies.api.entity.ai.statemachine.states.IState;

/**
 * Type for one time usage events
 */
public interface IStateMachineOneTimeEvent<S extends IState> extends IStateMachineEvent<S>
{
    /**
     * Check whether the one time Event is done
     *
     * @return true when ready to be removed
     */
    boolean shouldRemove();
}
