package com.minecolonies.api.sounds;

/**
 * All possible sound events.
 */
public enum EventType
{
    GENERAL(0.2, "general"),
    NOISE(2, "noise"),
    OFF_TO_BED(2, "gotobed"),
    BAD_WEATHER(2, "badweather"),
    SATURATION_LOW(0.2, "lowsaturation"),
    SATURATION_HIGH(0.2, "highsaturation"),
    BAD_HOUSING(0.2, "badhousing"),
    GOOD_HOUSING(0.2, "goodhousing"),

    GREETING(2, "greeting"),
    FAREWELL(2, "farewell"),
    MISSING_EQUIPMENT(2, "missingequipment"),
    HAPPY(0.2, "happy"),
    UNHAPPY(0.2, "unhappy"),
    SICKNESS(0.2, "sick"),
    INTERACTION(100, "interaction"),
    SUCCESS(20, "success"),
    DANGER(2, "danger");

    /**
     * The id of it.
     */
    private final String id;

    /**
     * The chance for the sound to get played.
     */
    private double chance;

    /**
     * Create a sound event.
     *
     * @param chance the chance for the sound to be played.
     */
    EventType(final double chance, final String id)
    {
        this.chance = chance;
        this.id = id;
    }

    /**
     * Get the chance for the sound to play.
     *
     * @return the chance.
     */
    public double getChance()
    {
        return chance;
    }

    /**
     * Get the id.
     * @return the id.
     */
    public String getId()
    {
        return this.id;
    }
}
