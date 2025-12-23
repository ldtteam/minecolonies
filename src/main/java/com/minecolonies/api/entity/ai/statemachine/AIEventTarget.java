package com.minecolonies.api.entity.ai.statemachine;

import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.IBooleanConditionSupplier;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.IStateSupplier;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Special AI Targets which are used for preState cecks and limits. They are checked before normal AITargets always
 */
public class AIEventTarget<S extends IState> extends TickingEvent<S>
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
        @NotNull final IBooleanConditionSupplier predicate,
        @NotNull final IStateSupplier<S> action, final int tickRate)
    {
        super(eventType, predicate, action, tickRate);
    }

    public AIEventTarget(
      @NotNull final AIBlockingEventType eventType,
        @NotNull final IBooleanConditionSupplier predicate,
      @NotNull final S IAIState,
      final int tickRate)
    {
        super(eventType, predicate, () -> IAIState, tickRate);
    }

    public AIEventTarget(
      @NotNull final AIBlockingEventType eventType,
        @NotNull final IStateSupplier<S> action,
      final int tickRate)
    {
        super(eventType, () -> true, action, tickRate);
    }
}
