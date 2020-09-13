package com.minecolonies.api.entity.pathfinding;

/**
 * Implemented by entities the stuck handler takes care of, used to ask the AI if it possibly could be stuck
 */
public interface IStuckHandlerEntity
{
    /**
     * Check whether the entity could currently be stuck, return false to prevent the stuck handler from taking action
     *
     * @return true if so.
     */
    default public boolean canBeStuck()
    {
        return true;
    }
}
