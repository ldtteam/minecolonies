package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.states.IStateEventType;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineOneTimeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * One time event that can be checked at the given tickrate, one time events are removed after causing a state transition.
 * (Even when transitioning into the same state again)
 */
public class TickingOneTimeEvent<S extends IState> extends TickingEvent<S> implements IStateMachineOneTimeEvent<S>
{
    /**
     * Creates a new TickingEvent
     *
     * @param eventType The type of the event
     * @param condition condition when the event applies
     * @param nextState state the event transitions into
     * @param tickRate  tickrate at which the event is checked
     */
    protected TickingOneTimeEvent(
      @NotNull final IStateEventType eventType,
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<S> nextState,
      @NotNull final int tickRate)
    {
        super(eventType, condition, nextState, tickRate);
    }

    /**
     * Return true when it should be removed after transitioning to a state.
     */
    @Override
    public boolean shouldRemove()
    {
        return true;
    }
}
