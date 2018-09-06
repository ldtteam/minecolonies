package com.minecolonies.api.util.constant;

import net.minecraft.potion.Potion;

/**
 * Barbarian constants class.
 */
public final class BarbarianConstants
{
    /**
     * The amount of EXP to drop on entity death.
     */
    public static final int BARBARIAN_EXP_DROP = 1;

    /**
     * The range for the barb to move away.
     */
    public static final int    MOVE_AWAY_RANGE     = 4;

    public static final int BARBARIAN_HORDE_DIFFICULTY_FIVE = 5;

    /**
     * Values used to choose whether or not to play sound
     */
    public static final int OUT_OF_ONE_HUNDRED = 100;

    public static final int ONE = 1;

    /**
     * Values used for sword effect.
     */
    public static final Potion SPEED_EFFECT                = Potion.getPotionById(1);
    public static final int    TIME_TO_COUNTDOWN           = 240;
    public static final int    COUNTDOWN_SECOND_MULTIPLIER = 4;
    public static final int    SPEED_EFFECT_DISTANCE       = 7;
    public static final int    SPEED_EFFECT_DURATION       = 240;
    public static final int    SPEED_EFFECT_MULTIPLIER     = 2;

    /**
     * Amount of ladders to place before destroying blocks.
     */
    public static final int LADDERS_TO_PLACE = 10;

    /**
     * Amount of ticks to despawn the barbarian.
     */
    public static final int TICKS_TO_DESPAWN = Constants.TICKS_SECOND * Constants.SECONDS_A_MINUTE * 10;

    /**
     * Randomly execute it every this ticks.
     */
    public static final int EVERY_X_TICKS = 20;

    /**
     * Private constructor to hide implicit one.
     */
    private BarbarianConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
