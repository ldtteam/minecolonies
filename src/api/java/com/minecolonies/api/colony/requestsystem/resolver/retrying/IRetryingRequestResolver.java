package com.minecolonies.api.colony.requestsystem.resolver.retrying;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.request.IRetryableRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IQueuedRequestResolver;

public interface IRetryingRequestResolver extends IQueuedRequestResolver<IRetryableRequest>
{
    /**
     * Method to get the maximal amount of tries that is resolver attempts.
     * @return The maximal amount of tries.
     */
    int getMaximalTries();


}
