package com.minecolonies.api.quests;

/**
 * Custom return data for triggers. This allows a trigger to return things like the building or citizen it was supposed to check on.
 */
public interface ITriggerReturnData<T>
{
    /**
     * Check if it is a match.
     * @return true if so.
     */
    boolean isPositive();

    /**
     * Get the actual content of the return data.
     * @return the content.
     */
    T getContent();
}
