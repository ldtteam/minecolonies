package com.minecolonies.api.entity.ai.statemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A simple target the AI tries to accomplish. It has a state matcher, so it only gets executed on matching state. It has a tester function to make more checks to tell if execution
 * is wanted. And it can change state.
 */
public class AITarget<S extends IState> extends TickingTransition<S>
{
    /**
     * Construct a target.
     *
     * @param state     the state it needs to be
     * @param predicate the predicate for execution
     * @param action    the action to apply
     * @param tickRate  the tick rate.
     */
    public AITarget(
      @NotNull final S state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<S> action,
      final int tickRate)
    {
        super(state, predicate, action, tickRate);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     * @param tickRate  the tick rate.
     */
    protected AITarget(
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<S> action,
      final int tickRate)
    {
        super(predicate, action, tickRate);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     * @param tickRate       the tick rate.
     */
    public AITarget(@NotNull final S predicateState, @Nullable final S state, final int tickRate)
    {
        this(predicateState, () -> state, tickRate);
    }

    /**
     * Construct a target.
     *
     * @param state    the state it needs to be | null
     * @param action   the action to apply
     * @param tickRate the tick rate.
     */
    public AITarget(@NotNull final S state, @NotNull final Supplier<S> action, final int tickRate)
    {
        this(state, () -> true, action, tickRate);
    }
}
