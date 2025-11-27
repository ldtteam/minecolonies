package com.minecolonies.core.colony.events.raid;

import com.minecolonies.api.util.constant.Constants;

/**
 * Barbarian constants class.
 */
public final class RaiderConstants
{
    /**
     * The amount of EXP to drop on entity death.
     */
    public static final int BARBARIAN_EXP_DROP = 5;

    public static final int BARBARIAN_HORDE_DIFFICULTY_FIVE = 5;

    /**
     * Values used to choose whether or not to play sound
     */
    public static final int OUT_OF_ONE_HUNDRED = 100;

    public static final int ONE = 1;

    /**
     * Chief sword ability constants
     */
    public static final double CHIEF_SWORD_SPEED_DIFFICULTY = 2.0;
    public static final int    TIME_TO_COUNTDOWN            = 120;
    public static final int    SPEED_EFFECT_DISTANCE       = 7;
    public static final int    SPEED_EFFECT_DURATION       = 60;
    public static final int    SPEED_EFFECT_MULTIPLIER     = 1;

    /**
     * Amount of ticks to despawn the barbarian.
     */
    public static final int TICKS_TO_DESPAWN = Constants.TICKS_SECOND * Constants.SECONDS_A_MINUTE * 30;

    /**
     * Randomly execute it every this ticks.
     */
    public static final int EVERY_X_TICKS = 20;

    /**
     * Barbarian Attack Damage.
     */
    public static final double ATTACK_DAMAGE = 2.0D;

    /**
     * Raiders environmental resistance
     */
    public static final int BASE_ENV_DAMAGE_RESIST = 2;

    /**
     * Values used in Spawn() method
     */
    public static final double WHOLE_CIRCLE = 360.0;

    /**
     * Values used for AI Task's Priorities.
     */
    public static final int PRIORITY_ZERO  = 0;
    public static final int PRIORITY_ONE   = 1;
    public static final int PRIORITY_TWO   = 2;
    public static final int PRIORITY_THREE = 3;
    public static final int PRIORITY_FOUR  = 4;
    public static final int PRIORITY_FIVE  = 5;
    public static final int PRIORITY_SIX   = 6;

    /**
     * Other various values used for AI Tasks.
     */
    public static final double AI_MOVE_SPEED      = 2.0D;
    public static final float  MAX_WATCH_DISTANCE                     = 8.0F;
    public static final double MAX_MELEE_RAIDER_PERSECUTION_DISTANCE  = 64;
    public static final double MAX_ARCHER_RAIDER_PERSECUTION_DISTANCE = MAX_MELEE_RAIDER_PERSECUTION_DISTANCE + 16;

    /**
     * Values used for mob attributes.
     */
    public static final double FOLLOW_RANGE                = 35.0D;
    public static final double MOVEMENT_SPEED              = 0.25D;
    public static final double ARMOR                       = 1D;
    public static final double CHIEF_BONUS_ARMOR           = 2D;
    public static final double BARBARIAN_BASE_HEALTH       = 10;
    public static final double BARBARIAN_HEALTH_MULTIPLIER = 0.025;

    /**
     * Extended melee reach based on difficulty
     */
    public static final double EXTENDED_REACH_DIFFICULTY = 1.9;
    public static final double EXTENDED_REACH            = 0.4;
    public static final double MIN_DISTANCE_FOR_ATTACK   = 2.5;

    /**
     * Attack delay
     */
    public static final int MELEE_ATTACK_DELAY = 30;

    /**
     * Additional melee movement speed difficulty
     */
    public static final double ADD_SPEED_DIFFICULTY = 2.3;
    public static final double BONUS_SPEED          = 1.2;
    public static final double BASE_COMBAT_SPEED    = 1.2;


    /**
     * Private constructor to hide implicit one.
     */
    private RaiderConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
