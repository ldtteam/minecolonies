package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.StandardRequestManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StandardRetryingRequestResolver implements IRetryingRequestResolver
{

    private IRequestManager manager;
    private ILocation location;
    private IToken id;
    private IToken                   current;
    private HashMap<IToken, Integer> delays = new HashMap<>();
    private HashMap<IToken, Integer> assignedRequests = new HashMap<>();

    public StandardRetryingRequestResolver(final IFactoryController factoryController, final IRequestManager manager) {
        this.updateManager(manager);

        this.id = factoryController.getNewInstance(TypeConstants.ITOKEN, manager.getColony().getID());
        this.location = factoryController.getNewInstance(TypeConstants.ILOCATION, manager.getColony().getCenter(), manager.getColony().getWorld().provider.getDimension());

    }

    public StandardRetryingRequestResolver(final IToken id, final ILocation location)
    {
        this.id = id;
        this.location = location;
    }

    @Override
    public void updateManager(final IRequestManager manager)
    {
        this.manager = manager;
    }

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

    @Nullable
    @Override
    public IToken getCurrentlyBeingReassignedRequest()
    {
        return current;
    }

    @Override
    public int getCurrentReassignmentAttempt()
    {
        return isReassigning() ? -1 : assignedRequests.get(getCurrentlyBeingReassignedRequest()) + 1;
    }

    @Override
    public ImmutableList<IToken> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests.keySet());
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
        return getCurrentlyBeingReassignedRequest() == null || requestToCheck.getToken() != getCurrentlyBeingReassignedRequest() || getCurrentReassignmentAttempt() < getMaximalTries();
    }

    @Nullable
    @Override
    public List<IToken> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public void resolve(
                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws RuntimeException
    {
        delays.put(request.getToken(), getMaximalDelayBetweenRetriesInTicks());

        if (assignedRequests.containsKey(request.getToken()))
        {
            assignedRequests.put(request.getToken(), assignedRequests.get(request.getToken()) + 1);
            return;
        }

        assignedRequests.put(request.getToken(), 1);
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> completedRequest)
    {
        //Gets never called, since these will never get completed, only overruled or reassigned.
        return null;
    }

    @Nullable
    @Override
    public IRequest onRequestCancelledOrOverruled(
                                                   @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws IllegalArgumentException
    {
        //Okey somebody completed it or what ever.
        //Lets remove if from our data structures:
        if (assignedRequests.containsKey(request.getToken()))
        {
            delays.remove(request.getToken());
            assignedRequests.remove(request.getToken());
        }

        //No further processing needed.
        return null;
    }

    @Override
    public int getPriority()
    {
        return AbstractRequestResolver.CONST_DEFAULT_RESOLVER_PRIORITY - 50;
    }

    @Override
    public IToken getRequesterId()
    {
        return id;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return location;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        //Noop, we do not schedule child requests. So this is never called.
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {
        //Noop, see onRequestComplete.
    }

    @Override
    public void update()
    {
        StandardRequestManager.LogHandler.log("Starting reassignment.");

        //Lets decrement all delays
        getAllAssignedRequests().forEach(t -> {
            Integer current = delays.remove(t);
            delays.put(t, --current);
        });

        //Lets get all keys with 0 residual delay:
        final Set<IToken> retryables = delays.keySet().stream().filter(t -> delays.get(t) == 0).collect(Collectors.toSet());
        final Set<IToken> successfully = retryables.stream().filter(t -> {
            final Set<IToken> blackList = assignedRequests.get(t) < getMaximalTries() ? ImmutableSet.of() : ImmutableSet.of(id);

            this.setCurrent(t);
            final IToken resultingResolver = manager.reassignRequest(t, blackList);
            this.setCurrent(null);

            if (resultingResolver != null && !resultingResolver.equals(getRequesterId()))
            {
                assignedRequests.remove(t);
                delays.remove(t);
            }

            return resultingResolver != null;
        }).collect(Collectors.toSet());

        successfully.forEach(t -> {
            StandardRequestManager.LogHandler.log("Failed to reassign a retryable request: " + id);
        });

        StandardRequestManager.LogHandler.log("Finished reassignment.");
    }

    public void setCurrent(@Nullable final IToken token)
    {
        this.current = token;
    }

    public void updateData(@NotNull final Map<IToken, Integer> newAssignedRequests, @NotNull final Map<IToken, Integer> newDelays)
    {
        this.assignedRequests.clear();
        this.assignedRequests.putAll(newAssignedRequests);

        this.delays.clear();
        this.delays.putAll(newDelays);
    }

    public HashMap<IToken, Integer> getDelays()
    {
        return delays;
    }

    public HashMap<IToken, Integer> getAssignedRequests()
    {
        return assignedRequests;
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IToken token)
    {
        return new TextComponentString("Player");
    }
}
