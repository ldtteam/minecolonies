package com.minecolonies.api.colony.jobs;

/**
 * Interface for workers that make use of the colony flag (e.g. the knight).
 */
public interface IJobWithColonyFlag
{
    /**
     * What to do when the colony flag changes.
     */
    void onColonyFlagChanged();
}
