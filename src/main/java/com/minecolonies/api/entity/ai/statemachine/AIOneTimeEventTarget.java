package com.minecolonies.api.entity.ai.statemachine;

import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingOneTimeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * One time usage AITarget, unregisters itself after usage
 */
public class AIOneTimeEventTarget<S extends IState> extends TickingOneTimeEvent<S>
{
    /**
     * Event to trigger a one time transition.
     *
     * @param predicate which has to be true to execute
     * @param action    Supplier for the state to transition into
     */
    public AIOneTimeEventTarget(@NotNull final BooleanSupplier predicate, @NotNull final Supplier<S> action)
    {
        super(AIBlockingEventType.EVENT, predicate, action, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param predicate which has to be true to execute
     * @param state     state to transition into
     */
    public AIOneTimeEventTarget(@NotNull final BooleanSupplier predicate, @NotNull final S state)
    {
        super(AIBlockingEventType.EVENT, predicate, () -> state, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param action Supplier for the state to transition into
     */
    public AIOneTimeEventTarget(@NotNull final Supplier<S> action)
    {
        super(AIBlockingEventType.EVENT, () -> true, action, 1);
    }

    /**
     * Event to trigger a one time transition.
     *
     * @param state state to transition into
     */
    public AIOneTimeEventTarget(@NotNull final S state)
    {
        super(AIBlockingEventType.EVENT, () -> true, () -> state, 1);
    }
}
