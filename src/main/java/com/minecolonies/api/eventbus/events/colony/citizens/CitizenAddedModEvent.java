package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizenData;

/**
 * Event for when a citizen was added to the colony.
 */
public final class CitizenAddedModEvent extends AbstractCitizenModEvent
{
    /**
     * The way the citizen came into the colony.
     */
    private final CitizenAddedSource source;

    /**
     * Citizen added event.
     *
     * @param citizen the citizen related to the event.
     * @param source  the way the citizen came into the colony.
     */
    public CitizenAddedModEvent(final ICitizenData citizen, final CitizenAddedSource source)
    {
        super(citizen);
        this.source = source;
    }

    /**
     * Get the way the citizen came into the colony.
     *
     * @return the enum value.
     */
    public CitizenAddedSource getSource()
    {
        return source;
    }

    /**
     * How the citizen came into the colony.
     */
    public enum CitizenAddedSource
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
