package com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to handle internal state changes that might cause a loop.
 * Simply returns without notifying its wrapped manager about the state change.
 */
public final class WrappedStaticStateRequestManager extends AbstractWrappedRequestManager
{

    public WrappedStaticStateRequestManager(@NotNull final IStandardRequestManager wrappedManager)
    {
        super(wrappedManager);
    }

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @Override
    public void updateRequestState(@NotNull final IToken token, @NotNull final RequestState state) throws IllegalArgumentException
    {
        //TODO: implement when link is created with worker
    }
}
