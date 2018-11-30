package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.entity.ai.statemachine.states.AIState;
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
     * The max delay one can set upon AITarget creation
     */
    public static final int MAX_AI_TICKRATE         = 500;
    /**
     * Maximum of the random offset for AI Ticks, to not activate on the same tick.
     */
    public static final int MAX_AI_TICKRATE_VARIANT = 50;

    /**
     * The tickrate at which the Target should be called, e.g. tickRate = 20 means call function every 20 Ticks
     */
    @NotNull
    private        int tickRate;
    /**
     * The random offset for Ticks, so that AITargets get more distributed activations on server ticks
     */
    @NotNull
    private final  int tickOffset;
    /**
     * The variant used upon creation of the AITarget to uniformly distribute the Tick offset
     * Static variable counter that changes with each AITarget creation and affects the next one.
     */
    private static int tickOffsetVariant = 0;

    /**
     * Construct a target.
     * @param state     the state it needs to be | null
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(
      @NotNull final AIState state,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<AIState> action,
      @NotNull final int tickRate)
    {
        this.state = state;
        this.predicate = predicate;
        this.action = action;

        // Limit rates
        this.tickRate = tickRate > MAX_AI_TICKRATE ? MAX_AI_TICKRATE : tickRate;
        this.tickRate = this.tickRate < 1 ? 1 : this.tickRate;

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickOffsetVariant % this.tickRate;
        // Increase variant for next AITarget and reset variant at a certain point
        tickOffsetVariant++;
        if (tickOffsetVariant >= MAX_AI_TICKRATE_VARIANT)
        {
            tickOffsetVariant = 0;
        }
    }

    /**
     * Construct a null state target for subclasses. Only this constructor should be for allowed for targets without states.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    protected AITarget(
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<AIState> action,
      @NotNull final int tickRate)
    {
        this.state = null;
        this.predicate = predicate;
        this.action = action;

        // Limit rates
        this.tickRate = tickRate > MAX_AI_TICKRATE ? MAX_AI_TICKRATE : tickRate;
        this.tickRate = this.tickRate < 1 ? 1 : this.tickRate;

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickOffsetVariant % this.tickRate;
        // Increase variant for next AITarget and reset variant at a certain point
        tickOffsetVariant++;
        if (tickOffsetVariant >= MAX_AI_TICKRATE_VARIANT)
        {
            tickOffsetVariant = 0;
        }
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state)
    {
        this(predicateState, () -> state, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, @NotNull final int tickRate)
    {
        this(predicateState, () -> state, tickRate);
    }

    /**
     * Construct a target.
     * TODO: Remove once all Targets transitioned to tickRate
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, @NotNull final Supplier<AIState> action)
    {
        this(state, () -> true, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, @NotNull final Supplier<AIState> action, @NotNull final int tickRate)
    {
        this(state, () -> true, action, tickRate);
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
     * Never unregister persistent AITargets
     *
     * @return false
     */
    public boolean shouldUnregister()
    {
        return false;
    }

    /**
     * Returns the intended tickRate of the AITarget
     *
     * @return Tickrate
     */
    public int getTickRate()
    {
        return tickRate;
    }

    /**
     * Allow to dynamically change the tickrate
     *
     * @param tickRate rate at which the AITarget should tick
     */
    public void setTickRate(@NotNull final int tickRate)
    {
        this.tickRate = tickRate;
    }

    /**
     * Returns a random offset to Ticks
     *
     * @return random
     */
    public int getTickOffset()
    {
        return tickOffset;
    }
}
