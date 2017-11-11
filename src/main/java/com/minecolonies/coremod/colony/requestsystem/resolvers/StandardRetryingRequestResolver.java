package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.TypeConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StandardRetryingRequestResolver implements IRetryingRequestResolver
{

    private Set<IToken> assignedRequests = new HashSet<>();

    @Override
    public int getMaximalTries()
    {
        return Configurations.requestSystem.maximalRetries;
    }

    @Override
    public int getMaximalDelayBetweenRetriesInTicks()
    {
        return Configurations.requestSystem.delayBetweenRetries;
    }

    @Override
    public ImmutableList<IToken> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests);
    }

    @Override
    public TypeToken<? extends IRetryable> getRequestType()
    {
        return TypeConstants.RETRYABLE;
    }

    @Override
    public boolean canResolve(
                               @NotNull final IRequestManager manager, final IRequest<? extends IRetryable> requestToCheck)
    {
        return true;
    }

    @Nullable
    @Override
    public List<IToken> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        return null;
    }

    @Nullable
    @Override
    public void resolve(
                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws RuntimeException
    {

    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public IRequest onRequestCancelledOrOverruled(
                                                   @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws IllegalArgumentException
    {
        return null;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public IToken getRequesterId()
    {
        return null;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return null;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {

    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {

    }

    @Override
    public void update()
    {

    }
}
