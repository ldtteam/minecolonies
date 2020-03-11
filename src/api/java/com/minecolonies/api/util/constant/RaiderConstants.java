package com.minecolonies.api.util.constant;

import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;

/**
 * Barbarian constants class.
 */
public final class RaiderConstants
{
    /**
     * The amount of EXP to drop on entity death.
     */
    public static final int BARBARIAN_EXP_DROP = 5;

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
    public static final Potion SPEED_EFFECT                = Potions.STRONG_SWIFTNESS;
    public static final int    TIME_TO_COUNTDOWN           = 240;
    public static final int    COUNTDOWN_SECOND_MULTIPLIER = 4;
    public static final int    SPEED_EFFECT_DISTANCE       = 7;
    public static final int    SPEED_EFFECT_DURATION       = 60;
    public static final int    SPEED_EFFECT_MULTIPLIER     = 1;

    /**
     * Amount of ladders to place before destroying blocks.
     */
    public static final int LADDERS_TO_PLACE = 10;

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
    public static final double ATTACK_DAMAGE = 1.0D;

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
    public static final double AI_MOVE_SPEED               = 2.0D;
    public static final float  MAX_WATCH_DISTANCE          = 8.0F;

    /**
     * Values used for mob attributes.
     */
    public static final double FOLLOW_RANGE                = 35.0D;
    public static final double MOVEMENT_SPEED              = 0.25D;
    public static final double ARMOR                       = 0.5D;
    public static final double CHIEF_ARMOR                 = 8D;
    public static final double BARBARIAN_BASE_HEALTH       = 15;
    public static final double BARBARIAN_HEALTH_MULTIPLIER = 0.2;
    public static final double ATTACK_SPEED_DIVIDER        = 3;


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
