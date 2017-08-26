package com.minecolonies.api.colony.requestsystem.resolver;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;

/**
 * Interface describing an object that is capable of constructing a specific {@link IRequestResolver}
 *
 * @param <Resolver> The type of {@link IRequestResolver} this factory can produce.
 */
public interface IRequestResolverFactory<Resolver extends IRequestResolver> extends IFactory<IToken, Resolver>
{
}
