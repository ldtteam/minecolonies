package com.minecolonies.coremod.quests.triggers;

/**
 * Custom return data for triggers.
 */
public interface ITriggerReturnData<T>
{
    /**
     * Check if it is a match.
     * @return true if so.
     */
    boolean isPositive();

    /**
     *
     * @return
     */
    T get();
}
