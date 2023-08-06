package com.minecolonies.api.colony.citizens.event;

import com.minecolonies.api.colony.ICitizenData;

/**
 * Event for when a citizen was added to the colony.
 */
public class CitizenAddedEvent extends AbstractCitizenEvent
{
    /**
     * The way the citizen came into the colony.
     */
    private final Source source;

    /**
     * Citizen added event.
     *
     * @param citizen the citizen related to the event.
     * @param source  the way the citizen came into the colony.
     */
    public CitizenAddedEvent(final ICitizenData citizen, final Source source)
    {
        super(citizen);
        this.source = source;
    }

    /**
     * Get the way the citizen came into the colony.
     *
     * @return the enum value.
     */
    public Source getSource()
    {
        return source;
    }

    /**
     * How the citizen came into the colony.
     */
    public enum Source
    {
        /**
         * The citizen spawned as part of the {@link com.minecolonies.api.configuration.ServerConfiguration#initialCitizenAmount}.
         */
        INITIAL,
        /**
         * The citizen was born naturally.
         */
        BORN,
        /**
         * The citizen was hired from the tavern.
         */
        HIRED,
        /**
         * The citizen got resurrected from his grave.
         */
        RESURRECTED,
        /**
         * The citizen was spawned in using commands.
         */
        COMMANDS
    }
}
