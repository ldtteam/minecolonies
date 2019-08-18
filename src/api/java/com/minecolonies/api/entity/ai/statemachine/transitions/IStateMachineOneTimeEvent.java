package com.minecolonies.api.entity.ai.statemachine.transitions;

/**
 * Type for one time usage events
 */
public interface IStateMachineOneTimeEvent extends IStateMachineEvent
{
    /**
     * Check whether the one time Event is done
     *
     * @return true when ready to be removed
     */
    boolean shouldRemove();
}
