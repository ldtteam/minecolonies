package com.minecolonies.api.colony.requestsystem.resolver;

import com.google.common.collect.ImmutableCollection;
import com.minecolonies.api.colony.requestsystem.token.IToken;

/**
 * Interface used to describe a class that provides resolvers.
 * Should be put on Buildings, Citizens etc who can resolve certain requests.
 * <p>
 * If a provider is added to his or her colony
 */
public interface IRequestResolverProvider
{

    /**
     * Unique token identifying this provider inside the request management system.
     *
     * @return the token.
     */
    IToken getToken();

    /**
     * Method to get the resolvers that this provider provides.
     *
     * @return a list of resolvers.
     */
    ImmutableCollection<IRequestResolver<?>> getResolvers();
}
