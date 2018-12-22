package com.minecolonies.coremod.entity.ai.statemachine.tickingStateMachine;

public abstract class TickRateConstants
{
    /**
     * The max delay one can set upon AITarget creation
     */
    public static final int MAX_TICKRATE         = 500;
    /**
     * Maximum of the random offset for AI Ticks, to not activate on the same tick.
     */
    public static final int MAX_TICKRATE_VARIANT = 50;
}
