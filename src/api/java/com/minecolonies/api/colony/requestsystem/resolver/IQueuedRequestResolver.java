package com.minecolonies.api.colony.requestsystem.resolver;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.token.IToken;

public interface IQueuedRequestResolver<R> extends IRequestResolver<R>
{
    /**
     * Method to get a list of all assigned tokens to this resolver.
     * @return A list of all assigned tokens.
     */
    ImmutableList<IToken> getAllAssignedRequests();
}
