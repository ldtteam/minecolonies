package com.minecolonies.api.util.constant;

import net.minecraft.util.Tuple;

/**
 * Constants used by the Guard AIs.
 */
public final class GuardConstants
{
    /**
     * Default vision range.
     */
    public static final int DEFAULT_VISION = 16;

    /**
     * Range a guard should be within of GuardPos.
     */
    public static final int GUARD_POS_RANGE = 3;

    /**
     * Range a guard should be within of Follow for Tight Formation.
     */
    public static final int GUARD_FOLLOW_TIGHT_RANGE = 8;

    /**
     * Range a guard should be within of Follow for Lose Formation.
     */
    public static final int GUARD_FOLLOW_LOSE_RANGE = 15;

    /**
     * Y search range.
     */
    public static final int Y_VISION = 10;

    /**
     * Experience to add when a mob is killed
     */
    public static final int EXP_PER_MOB_DEATH = 5;

    // -- Delays -- \\

    /**
     * Seconds to delay after prepare AI State.
     */
    public static final int PREPARE_DELAY_SECONDS = 5;

    /**
     * Minimum physical Attack delay in ticks, Monsters are immune for 10 ticks
     */
    public static final int PHYSICAL_ATTACK_DELAY_MIN = 10;

    /**
     * Physical Attack delay in ticks.
     */
    public static final int PHYSICAL_ATTACK_DELAY_BASE = 20;

    // -- Delays -- \\

    // -- Ranged Guard Stuff -- \\

    /**
     * The base distance for an attack in Blocks
     */
    public static final int BASE_DISTANCE_FOR_RANGED_ATTACK = 5;

    /**
     * Rangers maximum distance in blocks for an attack.(24 max arrow dist)
     */
    public static final int MAX_DISTANCE_FOR_RANGED_ATTACK = 24;

    /**
     * Ranger's base damage
     */
    public static final int RANGER_BASE_DMG = 2;

    /**
     * Flee distance from Target
     */
    public static final int RANGED_FLEE_SQDIST = 5;

    /**
     * Ranged attack velocity
     */
    public static final float RANGED_VELOCITY = (float) 1.6D;

    /**
     * Physical Attack delay in ticks.
     */
    public static final int RANGED_ATTACK_DELAY_BASE = 20;

    /**
     * Ranged hit chance devider.
     */
    public static final double HIT_CHANCE_DIVIDER = 15.0D;

    /**
     * Have to aim that bit higher to hit the target.
     */
    public static final double RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.20000000298023224D;

    // -- Ranged Guard Stuff -- \\

    // -- Knight Guard Stuff -- \\

    /**
     * Basic delay for the next Knight attack.
     */
    public static final int KNIGHT_ATTACK_DELAY_BASE = 32;

    /**
     * Basic bonus hp for knights
     */
    public static final int KNIGHT_HP_BONUS = 5;

    /**
     * This knight's max distance for attacking.
     */
    public static final int MAX_DISTANCE_FOR_ATTACK = 2;

    // -- Knight Guard Stuff -- \\

    // -- Physical Guard Stuff -- \\

    /**
     * Base physical damage.
     */
    public static final int BASE_PHYSICAL_DAMAGE = 3;

    // -- Physical Guard Stuff -- \\

    // -- Guard Movement -- \\

    /**
     * Quantity the worker should turn around all at once.
     */
    public static final double TURN_AROUND = 180D;

    /**
     * Normal volume at which sounds are played at.
     */
    public static final double BASIC_VOLUME = 1.0D;

    /**
     * Quantity to be moved to rotate the entity without actually moving.
     */
    public static final double MOVE_MINIMAL = 0.01D;

    // -- Guard Movement -- \\

    /**
     * Guard armor constants
     */
    public static final Tuple<Integer, Integer> LEATHER_LEVEL_RANGE = new Tuple<>(0, 99);
    public static final Tuple<Integer, Integer> GOLD_LEVEL_RANGE = new Tuple<>(0, 99);
    public static final Tuple<Integer, Integer> CHAIN_LEVEL_RANGE = new Tuple<>(0, 99);
    public static final Tuple<Integer, Integer> IRON_LEVEL_RANGE = new Tuple<>(5, 99);
    public static final Tuple<Integer, Integer> DIA_LEVEL_RANGE = new Tuple<>(15, 99);

    public static final Tuple<Integer, Integer> LEATHER_BUILDING_LEVEL_RANGE = new Tuple<>(1, 5);
    public static final Tuple<Integer, Integer> GOLD_BUILDING_LEVEL_RANGE = new Tuple<>(1, 5);
    public static final Tuple<Integer, Integer> CHAIN_BUILDING_LEVEL_RANGE = new Tuple<>(2, 5);
    public static final Tuple<Integer, Integer> IRON_BUILDING_LEVEL_RANGE = new Tuple<>(3, 5);
    public static final Tuple<Integer, Integer> DIA_BUILDING_LEVEL_RANGE = new Tuple<>(4, 5);

    public static final Tuple<Integer, Integer> SHIELD_LEVEL_RANGE = new Tuple<>(2, 99);
    public static final Tuple<Integer, Integer> SHIELD_BUILDING_LEVEL_RANGE = new Tuple<>(1, 5);

    /**
     * Private constructor to hide the implicit one.
     */
    private GuardConstants()
    {
        /*
         * Intentionally left empty.
         */
    }

}
