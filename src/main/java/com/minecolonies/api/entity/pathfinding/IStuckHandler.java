package com.minecolonies.api.entity.pathfinding;

import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * Stuck handler for pathing, gets called to check/deal with stuck status
 */
public interface IStuckHandler<NAV extends PathNavigation & IMinecoloniesNavigator>
{
    /**
     * Checks if the navigator is stuck
     *
     * @param navigator navigator to check
     */
    void checkStuck(final NAV navigator);

    void resetGlobalStuckTimers();

    /**
     * Returns the stuck level (0-9) indicating how long the entity is stuck and which stuck actions got used
     *
     * @return
     */
    public int getStuckLevel();
}
