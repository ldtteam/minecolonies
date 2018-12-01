package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Special AI Targets which are used for preState cecks and limits.
 * They are checked before normal AITargets always
 */
public class AISpecialTarget extends AITarget
{
    /**
     * Special state for this AITarget
     */
    private final AIBlockingEventType state;

    /**
     * Construct a special target.
     *
     * @param state       the AISpecial State
     * @param predicate   boolean predicate to check before executin the action
     * @param action      action supplier which returns the next state
     * @param tickRate    tickRate at which this target should be called
     */
    public AISpecialTarget(
      @NotNull final AIBlockingEventType state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<IAIState> action, @NotNull final int tickRate)
    {
        super(predicate, action, tickRate);
        this.state = state;
    }

    // TODO:Remove after giving all targets a tickrate
    public AISpecialTarget(
      @NotNull final AIBlockingEventType state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final IAIState IAIState)
    {
        super(predicate, () -> IAIState, 1);
        this.state = state;
    }

    // TODO:Remove after giving all targets a tickrate
    public AISpecialTarget(
      @NotNull final AIBlockingEventType state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<IAIState> action)
    {
        super(predicate, action, 1);
        this.state = state;
    }

    // TODO:Remove after giving all targets a tickrate
    public AISpecialTarget(
      @NotNull final AIBlockingEventType state,
      @NotNull final Supplier<IAIState> action)
    {
        super((BooleanSupplier) () -> true, action, 1);
        this.state = state;
    }

    /**
     * Get the special state
     *
     * @return Special state
     */
    public AIBlockingEventType getSpecialState()
    {
        return state;
    }
}
