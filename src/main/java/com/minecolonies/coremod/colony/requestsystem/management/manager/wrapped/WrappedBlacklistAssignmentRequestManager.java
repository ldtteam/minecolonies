package com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.RequestHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Class used to handle internal reassignment changes.
 * Take the given blacklist into account when it assigns the requests.
 */
public final class WrappedBlacklistAssignmentRequestManager extends AbstractWrappedRequestManager
{

    @NotNull
    private final Collection<IToken<?>> blackListedResolvers;

    public WrappedBlacklistAssignmentRequestManager(@NotNull final IStandardRequestManager wrappedManager, @NotNull final Collection<IToken<?>> blackListedResolvers)
    {
        super(wrappedManager);
        this.blackListedResolvers = blackListedResolvers;
    }

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @Override
    public void assignRequest(@NotNull final IToken token) throws IllegalArgumentException
    {
        RequestHandler.assignRequest(wrappedManager, RequestHandler.getRequest(wrappedManager, token), blackListedResolvers);
    }
}
