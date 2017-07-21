package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public abstract class AbstractRequestResolver<R> implements IRequestResolver<R>
{

    /**
     * The default priority of a resolver.
     * 100
     */
    protected static final int CONST_DEFAULT_RESOLVER_PRIORITY = 100;

    @NotNull
    private final ILocation location;

    @NotNull
    private final IToken token;

    public AbstractRequestResolver(@NotNull final ILocation location, @NotNull final IToken token)
    {
        this.location = location;
        this.token = token;
    }

    @Override
    public IToken getID()
    {
        return token;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @Override
    public int getPriority()
    {
        return CONST_DEFAULT_RESOLVER_PRIORITY;
    }
}
