package com.minecolonies.entity.ai;

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

    private final AIState           state;
    private final BooleanSupplier   predicate;
    private final Supplier<AIState> action;

    /**
     * Construct a traget.
     *
     * @param action the action to apply
     */
    public AITarget(Supplier<AIState> action)
    {
        this(() -> true, action);
    }

    /**
     * Construct a traget.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(BooleanSupplier predicate, Supplier<AIState> action)
    {
        this(null, predicate, action);
    }

    /**
     * Construct a traget.
     *
     * @param state     the state it needs to be | null
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(AIState state, BooleanSupplier predicate, Supplier<AIState> action)
    {
        this.state = state;
        this.predicate = predicate;
        this.action = action;
    }

    /**
     * Construct a traget.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(AIState state, Supplier<AIState> action)
    {
        this(state, () -> true, action);
    }

    /**
     * The state this target matches on.
     * Use null to match on all states.
     *
     * @return the state
     */
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
}
