package com.minecolonies.coremod.entity.ai.statemachine.tickingStateMachine;

import com.minecolonies.coremod.entity.ai.statemachine.states.IAIEventType;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.statemachine.transitions.IStateMachineOneTimeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TickingOneTimeEvent extends TickingEvent implements IStateMachineOneTimeEvent
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
      @NotNull final IAIEventType eventType,
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<IAIState> nextState,
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
