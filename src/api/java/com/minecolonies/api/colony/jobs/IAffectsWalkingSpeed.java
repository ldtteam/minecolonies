package com.minecolonies.api.colony.jobs;

/**
 * Interface for all jobs that somehow affect the walking speed.
 */
public interface IAffectsWalkingSpeed
{
    /**
     * Get the actual walking speed.
     * @return the speed.
     */
    double getWalkingSpeed();
}
