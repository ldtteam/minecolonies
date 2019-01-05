package com.minecolonies.coremod.entity.ai.statemachine.basestatemachine;

import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.statemachine.transitions.IStateMachineTransition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Basic Transition class for statemachines.
 * Consists of a state the transition applies in, a statesupplier which determines its next state and
 * a condition which has to be true to transition into the next state.
 */
public class BasicTransition implements IStateMachineTransition
{
    /**
     * The State we're starting in
     */
    @Nullable
    private final IAIState state;

    /**
     * The condition which needs to be met to transition
     */
    @NotNull
    private final BooleanSupplier condition;

    /**
     * The next state we transition into
     */
    @NotNull
    private final Supplier<IAIState> nextState;

    /**
     * Creating a new transition from State A to B under condition C
     *
     * @param state     State A
     * @param condition Condition C
     * @param nextState State B
     */
    public BasicTransition(@NotNull final IAIState state, @NotNull final BooleanSupplier condition, @NotNull final Supplier<IAIState> nextState)
    {
        this.state = state;
        this.condition = condition;
        this.nextState = nextState;
    }

    /**
     * Protected Constructor to allow subclasses without a state
     */
    protected BasicTransition(@NotNull final BooleanSupplier condition, @NotNull final Supplier<IAIState> nextState)
    {
        this.state = null;
        this.condition = condition;
        this.nextState = nextState;
    }

    /**
     * Returns the state to apply this transition in
     *
     * @return IAIState
     */
    public IAIState getState()
    {
        return state;
    }

    /**
     * Calculate the next state to go into
     *
     * @return next AI state
     */
    public IAIState getNextState()
    {
        return nextState.get();
    }

    /**
     * Check if the condition of this transition applies
     */
    public boolean checkCondition()
    {
        return condition.getAsBoolean();
    }
}
