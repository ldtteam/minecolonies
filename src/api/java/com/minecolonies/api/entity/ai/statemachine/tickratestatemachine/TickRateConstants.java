package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

/**
 * Constants for tickrate limited Transitions and statemachines.
 */
public class TickRateConstants
{
    /**
     * The max delay one can set upon AITarget creation
     */
    public static final int MAX_TICKRATE = 500;

    /**
     * Maximum of the random offset for AI Ticks, to not activate on the same tick.
     */
    public static final int MAX_TICKRATE_VARIANT = 50;
}
