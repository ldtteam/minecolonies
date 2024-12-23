package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract event for citizen related things.
 */
public class AbstractCitizenModEvent extends AbstractColonyModEvent
{
    /**
     * The citizen related to the event.
     */
    private final @NotNull ICitizenData citizen;

    /**
     * Constructs a citizen-based event.
     *
     * @param citizen the citizen related to the event.
     */
    protected AbstractCitizenModEvent(final @NotNull ICitizenData citizen)
    {
        super(citizen.getColony());
        this.citizen = citizen;
    }

    /**
     * Get the citizen related to the event.
     *
     * @return the citizen instance.
     */
    @NotNull
    public ICitizen getCitizen()
    {
        return citizen;
    }
}
