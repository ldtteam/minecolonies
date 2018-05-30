package com.minecolonies.coremod.entity.ai.citizen.guard;

/**
 * Constants used by the Guard AIs.
 */
public final class GuardConstants
{

    /**
     * Default vision range.
     */
    public static final int DEFAULT_VISION = 10;

    /**
     * Range a guard should be within of GuardPos.
     */
    public static final int GUARD_POS_RANGE = 0;

    /**
     * double damage threshold
     */
    public static final int DOUBLE_DAMAGE_THRESHOLD = 2;

    /**
     * Y search range.
     */
    public static final int Y_VISION = 10;

    /**
     * Experience to add when a mob is killed
     */
    public static final int EXP_PER_MOD_DEATH = 5;

    // -- Delays -- \\

    /**
     * Seconds to delay after prepare AI State.
     */
    public static final int PREPARE_DELAY_SECONDS = 5;

    // -- Delays -- \\

    // -- Sound Crap -- \\

    /**
     * The base pitch, add more to this to change the sound.
     */
    public static final double BASE_PITCH = 0.8D;

    /**
     * The pitch will be divided by this to calculate it for the arrow sound.
     */
    public static final double PITCH_DIVIDER = 1.0D;

    /**
     * Random is multiplied by this to get a random sound.
     */
    public static final double PITCH_MULTIPLIER = 0.4D;

    // -- Sound Crap -- \\

    // -- Ranged Guard Stuff -- \\

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
     * Private constructor to hide the implicit one.
     */
    private GuardConstants()
    {
        /**
         * Intentionally left empty.
         */
    }

}
