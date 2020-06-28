package com.minecolonies.api.entity.ai.statemachine;

import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Special AI Targets which are used for preState cecks and limits.
 * They are checked before normal AITargets always
 */
public class AIEventTarget extends TickingEvent<IAIState>
{
    /**
     * Construct a special target.
     *
     * @param eventType the AISpecial State
     * @param predicate boolean predicate to check before executin the action
     * @param action    action supplier which returns the next eventType
     * @param tickRate  tickRate at which this target should be called
     */
    public AIEventTarget(
      @NotNull final AIBlockingEventType eventType,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<IAIState> action, @NotNull final int tickRate)
    {
        super(eventType, predicate, action, tickRate);
    }

    public AIEventTarget(
      @NotNull final AIBlockingEventType eventType,
      @NotNull final BooleanSupplier predicate,
      @NotNull final IAIState IAIState,
      final int tickRate)
    {
        super(eventType, predicate, () -> IAIState, tickRate);
    }

    public AIEventTarget(
      @NotNull final AIBlockingEventType eventType,
      @NotNull final Supplier<IAIState> action,
      final int tickRate)
    {
        super(eventType, () -> true, action, tickRate);
    }
}
