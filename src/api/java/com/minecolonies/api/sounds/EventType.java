package com.minecolonies.api.sounds;

/**
 * All possible sound events.
 */
public enum EventType
{
    GENERAL(0.2),
    NOISE(2),
    OFF_TO_BED(2),
    BAD_WEATHER(2),
    SATURATION_LOW(0.2),
    SATURATION_HIGH(0.2),
    BAD_HOUSING(0.2),
    GREETING(2),
    FAREWELL(2),
    MISSING_EQUIPMENT(2),
    HAPPY(0.2),
    UNHAPPY(0.2),
    SICKNESS(0.2),
    INTERACTION(100),
    SUCCESS(20),
    DANGER(2);

    /**
     * The chance for the sound to get played.
     */
    private double chance;

    /**
     * Create a sound event.
     * @param chance the chance for the sound to be played.
     */
    EventType(final double chance)
    {
        this.chance = chance;
    }

    /**
     * Get the chance for the sound to play.
     * @return the chance.
     */
    public double getChance()
    {
        return chance;
    }
}
