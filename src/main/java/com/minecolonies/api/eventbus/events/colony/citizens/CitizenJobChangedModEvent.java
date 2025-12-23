package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 * Event for when a citizen their job changes.
 */
public final class CitizenJobChangedModEvent extends AbstractCitizenModEvent
{
    /**
     * The previous job.
     */
    private final JobEntry previousJob;

    /**
     * Citizen added event.
     *
     * @param citizen     the citizen related to the event.
     * @param previousJob the job the citizen had prior to the change.
     */
    public CitizenJobChangedModEvent(final ICitizenData citizen, final JobEntry previousJob)
    {
        super(citizen);
        this.previousJob = previousJob;
    }

    /**
     * Get the previous job of the citizen.
     *
     * @return the job entry instance.
     */
    public JobEntry getPreviousJob()
    {
        return previousJob;
    }
}
