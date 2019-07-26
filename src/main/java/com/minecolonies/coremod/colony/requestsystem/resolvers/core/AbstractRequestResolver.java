package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract implementation for the {@link IRequestResolver} interface that deals with most
 * of the internal basic logic.
 */
public abstract class AbstractRequestResolver<R extends IRequestable> implements IRequestResolver<R>
{
    @NotNull
    private final IToken<?> token;

    @NotNull
    private final ILocation location;

    @NotNull
    private final TypeToken<? extends R> requestType;

    public AbstractRequestResolver(
      @NotNull final IToken<?> token,
      @NotNull final ILocation location,
      @NotNull final TypeToken<? extends R> requestType)
    {
        this.token = token;
        this.location = location;
        this.requestType = requestType;
    }

    @Override
    public IToken<?> getId()
    {
        return token;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @NotNull
    @Override
    public TypeToken<? extends R> getRequestType()
    {
        return requestType;
    }
}
