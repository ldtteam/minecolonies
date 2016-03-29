package com.minecolonies.entity.ai;

import com.minecolonies.entity.ai.state.AIStateBase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;


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

    /**
     * Custom logger for the class.
     */
    private static final Logger log = Logger.getLogger(AITarget.class.getName());
    private final AIStateBase state;
    private final BooleanSupplier predicate;
    private final Supplier<AIStateBase> action;

    /**
     * Construct a traget.
     *
     * @param action the action to apply
     */
    public AITarget(Supplier<AIStateBase> action)
    {
        this(() -> true, action);
    }

    /**
     * Construct a traget.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(BooleanSupplier predicate, Supplier<AIStateBase> action)
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
    public AITarget(AIStateBase state, BooleanSupplier predicate, Supplier<AIStateBase> action)
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
    public AITarget(AIStateBase state, Supplier<AIStateBase> action)
    {
        this(state, () -> true, action);
    }

    /**
     * The state this target matches on.
     * Use null to match on all states.
     *
     * @return the state
     */
    public AIStateBase getState()
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
    public AIStateBase apply()
    {
        return action.get();
    }
}
