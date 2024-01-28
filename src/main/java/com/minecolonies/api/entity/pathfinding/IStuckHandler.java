package com.minecolonies.api.entity.pathfinding;

/**
 * Stuck handler for pathing, gets called to check/deal with stuck status
 */
public interface IStuckHandler
{
    /**
     * Checks if the navigator is stuck
     *
     * @param navigator navigator to check
     */
    void checkStuck(final AbstractAdvancedPathNavigate navigator);
}
