package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IAIEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Event with a tickrate for a statemachine using a tickrate.
 */
public class TickingEvent extends TickingTransition implements IStateMachineEvent
{
    /**
     * The type of this Event
     */
    private final IAIEventType eventType;

    /**
     * Creates a new TickingEvent
     *
     * @param eventType The type of the event
     * @param condition condition when the event applies
     * @param nextState state the event transitions into
     * @param tickRate  tickrate at which the event is checked
     */
    protected TickingEvent(
      @NotNull final IAIEventType eventType,
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<IAIState> nextState,
      @NotNull final int tickRate)
    {
        super(condition, nextState, tickRate);
        this.eventType = eventType;
    }

    /**
     * Get the type of this event
     */
    @Override
    public IAIEventType getEventType()
    {
        return eventType;
    }
}
