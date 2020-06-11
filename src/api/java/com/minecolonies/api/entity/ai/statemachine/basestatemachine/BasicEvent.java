package com.minecolonies.api.entity.ai.statemachine.basestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IStateEventType;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Basic event for statemachines, consists of a condition and a statesupplier to transition the statemachine into.
 * Events are always executed before any state transitions happen.
 */
public class BasicEvent extends BasicTransition<IAIState> implements IStateMachineEvent<IAIState>
{
    /**
     * The event type of this event
     */
    private final IStateEventType eventType;

    public BasicEvent(
      @NotNull final IStateEventType eventType,
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<IAIState> nextState)
    {
        super(condition, nextState);
        this.eventType = eventType;
    }

    /**
     * Get the Eventtype
     *
     * @return IAIEventType
     */
    public IStateEventType getEventType()
    {
        return eventType;
    }

    /**
     * Events do not have a state
     */
    public final IAIState getState()
    {
        return null;
    }
}
