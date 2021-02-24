package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.basestatemachine.BasicTransition;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE_VARIANT;

/**
 * Transition with tickrate logic, allows to define an intended tickrate at which this transition will be checked.
 */
public class TickingTransition<S extends IState> extends BasicTransition<S> implements ITickingTransition<S>
{
    /**
     * The tickrate at which the Target should be called, e.g. tickRate = 20 means call function every 20 Ticks
     */
    private int tickRate;

    /**
     * The random offset for Ticks, so that AITargets get more distributed activations on server ticks
     */
    private final int tickOffset;

    /**
     * The variant used upon creation of the AITarget to uniformly distribute the Tick offset Static variable counter that changes with each AITarget creation and affects the next
     * one.
     */
    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private static int tickOffsetVariant = 0;

    /**
     * Create a new Transition with tickrate
     *
     * @param state     State to apply the transition in
     * @param condition Condition checked before going to the next state
     * @param nextState The next state this transition leads into
     * @param tickRate  The expected tickrate at which this transition should be checked.
     */
    public TickingTransition(
      @NotNull final S state,
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<S> nextState,
      final int tickRate)
    {
        super(state, condition, nextState);

        // Limit rates
        this.tickRate = Math.min(tickRate, MAX_TICKRATE);
        this.tickRate = Math.max(this.tickRate, 1);

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickOffsetVariant % this.tickRate;
        // Increase variant for next AITarget and reset variant at a certain point
        tickOffsetVariant++;
        if (tickOffsetVariant >= MAX_TICKRATE_VARIANT)
        {
            tickOffsetVariant = 0;
        }
    }

    /**
     * Create a new Transition with tickrate
     *
     * @param condition Condition checked before going to the next state
     * @param nextState The next state this transition leads into
     * @param tickRate  The expected tickrate at which this transition should be checked.
     */
    public TickingTransition(
      @NotNull final BooleanSupplier condition,
      @NotNull final Supplier<S> nextState,
      final int tickRate)
    {
        super(condition, nextState);

        // Limit rates
        this.tickRate = Math.min(tickRate, MAX_TICKRATE);
        this.tickRate = Math.max(this.tickRate, 1);

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickOffsetVariant % this.tickRate;
        // Increase variant for next AITarget and reset variant at a certain point
        tickOffsetVariant++;
        if (tickOffsetVariant >= MAX_TICKRATE_VARIANT)
        {
            tickOffsetVariant = 0;
        }
    }

    /**
     * Returns the intended tickRate of the AITarget
     *
     * @return Tickrate
     */
    @Override
    public int getTickRate()
    {
        return tickRate;
    }

    /**
     * Allow to dynamically change the tickrate
     *
     * @param tickRate rate at which the AITarget should tick
     */
    @Override
    public void setTickRate(final int tickRate)
    {
        this.tickRate = tickRate;
    }

    /**
     * Returns a preset offset to Ticks
     *
     * @return random
     */
    @Override
    public int getTickOffset()
    {
        return tickOffset;
    }
}
