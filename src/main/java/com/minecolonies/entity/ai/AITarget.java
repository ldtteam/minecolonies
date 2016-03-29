package com.minecolonies.entity.ai;

import com.minecolonies.entity.ai.state.AIStateBase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;


/**
 * A simple target the AI tries to accomplish
 */
public class AITarget
{

    /**
     * Custom logger for the class.
     */
    private static final Logger LOGGER = Logger.getLogger(AITarget.class.getName());
    private final AIStateBase state;
    private final BooleanSupplier predicate;
    private final Supplier<AIStateBase> action;

    public AITarget(Supplier<AIStateBase> action)
    {
        this(() -> true, action);
    }

    public AITarget(AIStateBase state, Supplier<AIStateBase> action)
    {
        this(state, () -> true, action);
    }

    public AITarget(BooleanSupplier predicate, Supplier<AIStateBase> action)
    {
        this(null, predicate, action);
    }

    public AITarget(AIStateBase state, BooleanSupplier predicate, Supplier<AIStateBase> action)
    {
        this.state = state;
        this.predicate = predicate;
        this.action = action;
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

    public boolean test()
    {
        return predicate.getAsBoolean();
    }

    public AIStateBase apply()
    {
        return action.get();
    }
}
