package com.minecolonies.coremod.entity.ai.util;

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
public class AITarget
{

    @Nullable
    private final AIState           state;
    @NotNull
    private final BooleanSupplier   predicate;
    @NotNull
    private final Supplier<AIState> action;

    /**
     * Variable describing if it is okay to eat in a state.
     */
    private boolean okayToEat;

    /**
     * Construct a target.
     *
     * @param action the action to apply
     */
    public AITarget(@NotNull final Supplier<AIState> action, final boolean isOkayToEat)
    {
        this(() -> true, isOkayToEat, action);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(@NotNull final BooleanSupplier predicate, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(null, isOkayToEat, predicate, action);
    }

    /**
     * Construct a target.
     *
     * @param state     the state it needs to be | null
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final BooleanSupplier predicate, @NotNull final Supplier<AIState> action)
    {
        if (state == null)
        {
            this.state = AIState.NULLSTATE;
        }
        else
        {
            this.state = state;
        }
        this.predicate = predicate;
        this.action = action;
        this.okayToEat = isOkayToEat;
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param state     the state to switch to
     */
    public AITarget(@NotNull final BooleanSupplier predicate, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(null, isOkayToEat, predicate, () -> state);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(predicateState, isOkayToEat, () -> state);
    }

    /**
     * Construct a target.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(state, isOkayToEat, () -> true, action);
    }

    /**
     * The state this target matches on.
     * Use null to match on all states.
     *
     * @return the state
     */
    @Nullable
    public AIState getState()
    {
        return state;
    }

    /**
     * Return whether the ai wants this target to be executed.
     *
     * @return true if execution is wanted.
     */
    public boolean test()
    {
        return predicate.getAsBoolean();
    }

    /**
     * Execute this target.
     * Do some stuff and return the state transition.
     *
     * @return the new state the ai is in. null if no change.
     */
    public AIState apply()
    {
        return action.get();
    }

    /**
     * Called to see if it is okay for the citizen to eat when
     * in this state.
     *
     * @return indicates if it is Okay to eat in this state
     */
    public boolean isOkayToEat()
    {
        return okayToEat;
    }
}
