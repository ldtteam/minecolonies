package com.minecolonies.api.entity.ai.statemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A simple target the AI tries to accomplish.
 * It has a state matcher,
 * so it only gets executed on matching state.
 * It has a tester function to make more checks
 * to tell if execution is wanted.
 * And it can change state.
 */
public class AITarget extends TickingTransition<IAIState>
{
    /**
     * Construct a target.
     *
     * @param state     the state it needs to be
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(
      @NotNull final IAIState state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<IAIState> action,
      @NotNull final int tickRate)
    {
        super(state, predicate, action, tickRate);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    protected AITarget(
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<IAIState> action,
      @NotNull final int tickRate)
    {
        super(predicate, action, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final IAIState predicateState, @Nullable final IAIState state)
    {
        this(predicateState, () -> state, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final IAIState predicateState, @Nullable final IAIState state, @NotNull final int tickRate)
    {
        this(predicateState, () -> state, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@NotNull final IAIState state, @NotNull final Supplier<IAIState> action)
    {
        this(state, () -> true, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@NotNull final IAIState state, @NotNull final Supplier<IAIState> action, @NotNull final int tickRate)
    {
        this(state, () -> true, action, tickRate);
    }
}
