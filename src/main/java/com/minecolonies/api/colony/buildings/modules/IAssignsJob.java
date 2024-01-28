package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 * Interface for all modules that need special assignment handling.
 */
public interface IAssignsJob extends IAssignsCitizen
{
    /**
     * Check if there are any assigned citizens in the module.
     * @return true if so.
     */
    boolean hasAssignedCitizen();

    /**
     * Get the job entry of the module.
     * @return the job entry.
     */
    JobEntry getJobEntry();
}
