package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.LogHandler;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.RSConstants.CONST_RETRYING_RESOLVER_PRIORITY;
import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;

public class StandardRetryingRequestResolver implements IRetryingRequestResolver
{

    private static final Integer CONST_RETRYING_ID_SCALE = -20000;

    private       IRequestManager manager;
    private final ILocation       location;
    private final IToken<?>       id;
    private       IToken<?>       current;
    private final HashMap<IToken<?>, Integer> delays           = new HashMap<>();
    private final HashMap<IToken<?>, Integer> assignedRequests = new HashMap<>();

    public StandardRetryingRequestResolver(final IFactoryController factoryController, final IRequestManager manager)
    {
        this.updateManager(manager);

        this.id = factoryController.getNewInstance(TypeConstants.ITOKEN, manager.getColony().getID() * CONST_RETRYING_ID_SCALE);
        this.location = factoryController.getNewInstance(TypeConstants.ILOCATION, manager.getColony().getCenter(), manager.getColony().getDimension());
    }

    public StandardRetryingRequestResolver(final IToken<?> id, final ILocation location)
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

    @Override
    public int getCurrentReassignmentAttempt()
    {
        return isReassigning() ? -1 : assignedRequests.get(getCurrentlyBeingReassignedRequest()) + 1;
    }

    @Nullable
    @Override
    public IToken<?> getCurrentlyBeingReassignedRequest()
    {
        return current;
    }

    @Override
    public TypeToken<? extends IRetryable> getRequestType()
    {
        return TypeConstants.RETRYABLE;
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends IRetryable> requestToCheck)
    {
        return getCurrentlyBeingReassignedRequest() == null || requestToCheck.getToken() != getCurrentlyBeingReassignedRequest()
                 || getCurrentReassignmentAttempt() < getMaximalTries();
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        return ImmutableList.of();
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws RuntimeException
    {
        delays.put(request.getToken(), getMaximalDelayBetweenRetriesInTicks());
        assignedRequests.put(request.getToken(), assignedRequests.containsKey(request.getToken()) ? assignedRequests.get(request.getToken()) + 1 : 1);
    }

    @SuppressWarnings(RAWTYPES)
    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> completedRequest)
    {
        //Gets never called, since these will never get completed, only overruled or reassigned.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        if (assignedRequests.containsKey(request.getToken()))
        {
            delays.remove(request.getToken());
            assignedRequests.remove(request.getToken());
        }

        //No further processing needed.
        return null;
    }

    @Override
    public void onRequestBeingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        onRequestCancelled(manager, request);
    }

    @Override
    public int getPriority()
    {
        return CONST_RETRYING_RESOLVER_PRIORITY;
    }

    @Override
    public void update()
    {
        LogHandler.log("Starting reassignment.");

        //Lets decrement all delays
        getAllAssignedRequests().forEach(t -> {
            Integer current = delays.remove(t);
            delays.put(t, --current);
        });

        //Lets get all keys with 0 residual delay:
        final Set<IToken<?>> retryables = delays.keySet().stream().filter(t -> delays.get(t) == 0).collect(Collectors.toSet());
        final Set<IToken<?>> successfully = retryables.stream().filter(t -> {
            final Set<IToken<?>> blackList = assignedRequests.get(t) < getMaximalTries() ? ImmutableSet.of() : ImmutableSet.of(id);

            Integer currentAttempt = assignedRequests.get(t);

            this.setCurrent(t);
            final IToken<?> resultingResolver;

            try
            {
                resultingResolver = manager.reassignRequest(t, blackList);
            }
            catch (Exception ex)
            {
                assignedRequests.remove(t);
                delays.remove(t);
                return false;
            }

            this.setCurrent(null);

            assignedRequests.put(t, ++currentAttempt);

            if (resultingResolver != null && !resultingResolver.equals(getRequesterId()))
            {
                assignedRequests.remove(t);
                delays.remove(t);
            }

            return resultingResolver != null;
        }).collect(Collectors.toSet());

        successfully.forEach(t -> {
            LogHandler.log("Failed to reassign a retryable request: " + id);
        });

        LogHandler.log("Finished reassignment.");
    }

    @Override
    public ImmutableList<IToken<?>> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests.keySet());
    }

    @Override
    public void onSystemReset()
    {
        assignedRequests.clear();
        delays.clear();
    }

    public void setCurrent(@Nullable final IToken<?> token)
    {
        this.current = token;
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return id;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return location;
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //Noop, we do not schedule child requests. So this is never called.
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //Noop, see onRequestComplete.
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        return new TextComponentString("Player");
    }

    public void updateData(@NotNull final Map<IToken<?>, Integer> newAssignedRequests, @NotNull final Map<IToken<?>, Integer> newDelays)
    {
        this.assignedRequests.clear();
        this.assignedRequests.putAll(newAssignedRequests);

        this.delays.clear();
        this.delays.putAll(newDelays);
    }

    public Map<IToken<?>, Integer> getDelays()
    {
        return delays;
    }

    public Map<IToken<?>, Integer> getAssignedRequests()
    {
        return assignedRequests;
    }

    @Override
    public void onColonyUpdate(@NotNull final IRequestManager manager, @NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        new ArrayList<>(assignedRequests.keySet()).stream()
                .map(manager::getRequestForToken)
                .filter(shouldTriggerReassign)
                .filter(Objects::nonNull)
                .forEach(request ->
                {
                    final IToken newResolverToken = manager.reassignRequest(request.getToken(), ImmutableList.of(getRequesterId()));

                    if (newResolverToken != getRequesterId())
                    {
                        assignedRequests.remove(request.getToken());
                    }
                });
    }
}
