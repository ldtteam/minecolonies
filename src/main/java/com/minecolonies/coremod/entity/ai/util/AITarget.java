package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.api.util.constant.Constants;
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
     * The tickrate at which the Target should be called, e.g. tickRate = 20 means call function every 20 Ticks
     */
    @NotNull
    private int tickRate;

    /**
     * The random offset for Ticks, so that AITargets get more distributed activations on server ticks
     */
    @NotNull
    private final int tickOffset;

    /**
     * The variant used upon creation of the AITarget to uniformly distribute the Tick offset
     * Static variable counter that changes with each AITarget creation and affects the next one.
     */
    private static int tickVariant = 0;

    /**
     * Variable describing if it is okay to eat in a state.
     */
    private boolean okayToEat;

    /**
     * Construct a target.
     *
     * @param action the action to apply
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(@NotNull final Supplier<AIState> action, final boolean isOkayToEat, final int tickRate)
    {
        this(() -> true, isOkayToEat, action, tickRate);
    }

    /**
     * Default Tickrate - Construct a target.
     * TODO:Remove once classes are transitioned to the new system
     *
     * @param action the action to apply
     */
    public AITarget(@NotNull final Supplier<AIState> action, final boolean isOkayToEat)
    {
        this(() -> true, isOkayToEat, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(@NotNull final BooleanSupplier predicate, final boolean isOkayToEat, @NotNull final Supplier<AIState> action, final int tickRate)
    {
        this(null, isOkayToEat, predicate, action, tickRate);
    }

    /**
     * Default Tickrate - Construct a target.
     * TODO:Remove once classes are transitioned to the new system
     *
     * @param predicate the predicate for execution
     * @param action    the action to apply
     */
    public AITarget(@NotNull final BooleanSupplier predicate, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(null, isOkayToEat, predicate, action, 1);
    }

    /**
     * Construct a target.
     *
     * @param state     the state it needs to be | null
     * @param predicate the predicate for execution
     * @param action    the action to apply
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(
      @Nullable final AIState state,
      final boolean isOkayToEat,
      @NotNull final BooleanSupplier predicate,
      @NotNull final Supplier<AIState> action,
      @NotNull final int tickRate)
    {
        this.state = state;
        this.predicate = predicate;
        this.action = action;
        this.okayToEat = isOkayToEat;

        this.tickRate = tickRate > Constants.MAX_AI_TICKRATE ? Constants.MAX_AI_TICKRATE : tickRate;
        this.tickRate = this.tickRate < 1 ? 1 : this.tickRate;

        // Calculate offSet % tickRate already to not have redundant calculations later
        this.tickOffset = tickVariant % this.tickRate;

        // Increase variant for next AITarget and reset variant at a certain point
        tickVariant++;
        if (tickVariant >= Constants.MAX_AI_TICKRATE_VARIANT)
        {
            tickVariant = 0;
        }
    }

    /**
     * Construct a target.
     *
     * @param predicate the predicate for execution
     * @param state     the state to switch to
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(@NotNull final BooleanSupplier predicate, @Nullable final AIState state, final boolean isOkayToEat, @NotNull final int tickRate)
    {
        this(null, isOkayToEat, predicate, () -> state, tickRate);
    }

    /**
     * Default Tickrate - Construct a target.
     * TODO:Remove once classes are transitioned to the new system
     *
     * @param predicate the predicate for execution
     * @param state     the state to switch to
     */
    public AITarget(@NotNull final BooleanSupplier predicate, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(null, isOkayToEat, predicate, () -> state, 1);
    }

    /**
     * Construct a target.
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, final boolean isOkayToEat, @NotNull final int tickRate)
    {
        this(predicateState, isOkayToEat, () -> state, tickRate);
    }

    /**
     * Default Tickrate - Construct a target.
     * TODO:Remove once classes are transitioned to the new system
     *
     * @param predicateState the state it needs to be | null
     * @param state          the state to switch to
     */
    public AITarget(@NotNull final AIState predicateState, @Nullable final AIState state, final boolean isOkayToEat)
    {
        this(predicateState, isOkayToEat, () -> state, 1);
    }


    /**
     * Construct a target.
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     * @param tickRate the Tickrate to expect, e.g. 20 = called once every 20 ticks
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final Supplier<AIState> action, @NotNull final int tickRate)
    {
        this(state, isOkayToEat, () -> true, action, tickRate);
    }

    /**
     * Default Tickrate - Construct a target.
     * TODO:Remove once classes are transitioned to the new system
     *
     * @param state  the state it needs to be | null
     * @param action the action to apply
     */
    public AITarget(@Nullable final AIState state, final boolean isOkayToEat, @NotNull final Supplier<AIState> action)
    {
        this(state, isOkayToEat, () -> true, action, 1);
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
