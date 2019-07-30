package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.RSConstants.CONST_DEFAULT_RESOLVER_PRIORITY;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public abstract class AbstractRequestResolver<R extends IRequestable> implements IRequestResolver<R>
{
    @NotNull
    private final ILocation location;

    @NotNull
    private final IToken<?> token;

    public AbstractRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        this.location = location;
        this.token = token;
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
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        return new StringTextComponent("Request System");
    }

    @Override
    public int getPriority()
    {
        return CONST_DEFAULT_RESOLVER_PRIORITY;
    }
}
