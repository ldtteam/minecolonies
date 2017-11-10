package com.minecolonies.api.colony.requestsystem.resolver.retrying;

import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.IQueuedRequestResolver;

public interface IRetryingRequestResolver extends IQueuedRequestResolver<IRetryable>
{
    /**
     * Method to get the maximal amount of tries that is resolver attempts.
     * @return The maximal amount of tries.
     */
    int getMaximalTries();

    /**
     * Method to get the maximal ticks between retries.
     * @return The maximal amount of ticks between retries.
     */
    int getMaximalDelayBetweenRetriesInTicks();
}
