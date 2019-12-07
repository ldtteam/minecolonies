package com.minecolonies.api.colony.requestsystem.resolver;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;

public interface IQueuedRequestResolver<R extends IRequestable> extends IRequestResolver<R>
{
    /**
     * Method to get a list of all assigned tokens to this resolver.
     *
     * @return A list of all assigned tokens.
     */
    ImmutableList<IToken<?>> getAllAssignedRequests();

    /**
     * Called when the request system this is part of gets reset.
     */
    void onSystemReset();

    /**
     * Check if the resolver holds a certain request.
     * @param request the request to check.
     * @return true if so.
     */
    boolean holdsRequest(IToken request);
}
