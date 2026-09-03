package com.minecolonies.core.entity.other;

import com.minecolonies.core.entity.pathfinding.PathingOptions;

/**
 * A persistent mount that a citizen may ride while performing their job.
 */
public interface ICitizenJobMount
{
    /**
     * Applies mount-specific settings to the pathing options copied from the mount's navigator.
     *
     * @param options the pathing options for the mounted path job
     */
    default void configureMountedPathing(final PathingOptions options)
    {
        // No mount-specific pathing changes by default.
    }

    /**
     * Whether the mount recently collided horizontally while following its rider's path.
     *
     * @return true if the mount recently collided horizontally
     */
    default boolean hadHorizontalCollision()
    {
        return false;
    }
}
