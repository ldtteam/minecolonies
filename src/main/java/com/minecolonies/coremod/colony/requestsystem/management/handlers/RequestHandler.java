package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.manager.AssigningStrategy;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedBlacklistAssignmentRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to handle the inner workings of the request system with regards to requests.
 */
public class RequestHandler implements IRequestHandler
{

    private final IStandardRequestManager manager;

    public RequestHandler(final IStandardRequestManager manager) {this.manager = manager;}

    @Override
    public IRequestManager getManager()
    {
        return manager;
    }

    @Override
    @SuppressWarnings(UNCHECKED)
    public <Request extends IRequestable> IRequest<Request> createRequest(final IRequester requester, final Request request)
    {
        final IToken<UUID> token = manager.getTokenHandler().generateNewToken();

        final IRequest<Request> constructedRequest = manager.getFactoryController()
                                                       .getNewInstance(TypeToken.of((Class<? extends IRequest<Request>>) RequestMappingHandler.getRequestableMappings()
                                                                                                                           .get(request.getClass())), request, token, requester);

        manager.getLogger().debug("Creating request for: " + request + ", token: " + token + " and output: " + constructedRequest);

        registerRequest(constructedRequest);

        return constructedRequest;
    }

    @Override
    public void registerRequest(final IRequest<?> request)
    {
        if (manager.getRequestIdentitiesDataStore().getIdentities().containsKey(request.getId()) ||
              manager.getRequestIdentitiesDataStore().getIdentities().containsValue(request))
        {
            throw new IllegalArgumentException("The given request is already known to this manager");
        }

        manager.getLogger().debug("Registering request: " + request);

        manager.getRequestIdentitiesDataStore().getIdentities().put(request.getId(), request);
    }

    /**
     * Method used to assign a given request to a resolver. Does not take any blacklist into account.
     *
     * @param request The request to assign
     * @throws IllegalArgumentException when the request is already assigned
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    public void assignRequest(final IRequest<?> request)
    {
        assignRequest(request, Collections.emptyList());
    }

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
     *
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    public IToken<?> assignRequest(final IRequest<?> request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        switch (request.getStrategy())
        {
            case PRIORITY_BASED:
                return assignRequestDefault(request, resolverTokenBlackList);
            case FASTEST_FIRST:
            {
                Log.getLogger().warn("Fastest First strategy not implemented yet.");
                return assignRequestDefault(request, resolverTokenBlackList);
            }
        }

        return null;
    }

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
     * Uses the default assigning strategy: {@link AssigningStrategy#PRIORITY_BASED}
     *
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @Override
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    public IToken<?> assignRequestDefault(final IRequest request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        //Check if the request is registered
        getRequest(request.getId());

        manager.getLogger().debug("Starting resolver assignment search for request: " + request);

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.ASSIGNING);

        final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
        requestTypes.remove(TypeConstants.OBJECT);

        final List<TypeToken> typeIndexList = new LinkedList<>(requestTypes);

        final Set<IRequestResolver<?>> resolvers = requestTypes.stream()
                                                     .filter(typeToken -> manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().containsKey(typeToken))
                                                     .flatMap(type -> manager.getRequestableTypeRequestResolverAssignmentDataStore()
                                                                        .getAssignments()
                                                                        .get(type)
                                                                        .stream()
                                                                        .map(iToken -> manager.getResolverHandler().getResolver(iToken)))
                                                     .filter(iRequestResolver -> typeIndexList.contains(iRequestResolver.getRequestType()))
                                                     .sorted(Comparator.comparingInt((IRequestResolver<?> r) -> -1 * r.getPriority())
                                                               .thenComparingInt((IRequestResolver<?> r) -> typeIndexList.indexOf(r.getRequestType())))
                                                     .collect(Collectors.toCollection(LinkedHashSet::new));


        for (final IRequestResolver<?> resolver : resolvers)
        {
            //Skip when the resolver is in the blacklist.
            if (resolverTokenBlackList.contains(resolver.getId()))
            {
                continue;
            }

            //Skip if preliminary check fails
            if (!resolver.canResolveRequest(manager, request))
            {
                continue;
            }

            @Nullable final List<IToken<?>> attemptResult = resolver.attemptResolveRequest(new WrappedBlacklistAssignmentRequestManager(manager, resolverTokenBlackList), request);

            //Skip if attempt failed (aka attemptResult == null)
            if (attemptResult == null)
            {
                continue;
            }

            //Successfully found a resolver. Registering
            manager.getLogger().debug("Finished resolver assignment search for request: " + request + " successfully");

            manager.getResolverHandler().addRequestToResolver(resolver, request);
            //TODO: Change this false to simulation.
            resolver.onRequestAssigned(manager, request, false);

            for (final IToken<?> childRequestToken :
                            attemptResult)
            {
                @SuppressWarnings(RAWTYPES) final IRequest childRequest = manager.getRequestHandler().getRequest(childRequestToken);

                childRequest.setParent(request.getId());
                request.addChild(childRequest.getId());
            }

            for (final IToken<?> childRequestToken :
                            attemptResult)
            {
                @SuppressWarnings(RAWTYPES) final IRequest childRequest = manager.getRequestHandler().getRequest(childRequestToken);

                if (!isAssigned(childRequestToken))
                {
                    assignRequest(childRequest, resolverTokenBlackList);
                }
            }

            if (request.getState().ordinal() < RequestState.IN_PROGRESS.ordinal())
            {
                request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
                if (!request.hasChildren())
                {
                    resolveRequest(request);
                }
            }

            return resolver.getId();
        }

        return null;
    }

    /**
     * Method used to reassign the request to a resolver that is not in the given blacklist.
     * Cancels the request internally without notify the requester, and attempts a reassign. If the reassignment failed, it is assigned back to the orignal resolver.
     *
     * @param request                The request that is being reassigned.
     * @param resolverTokenBlackList The blacklist to which not to assign the request.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException Thrown when something went wrong.
     */
    @Override
    public IToken<?> reassignRequest(final IRequest<?> request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        if (request.hasChildren())
        {
            throw new IllegalArgumentException("Can not reassign a request that has children.");
        }

        final IRequestResolver currentlyAssignedResolver = manager.getResolverForRequest(request.getId());
        currentlyAssignedResolver.onAssignedRequestBeingCancelled(new WrappedStaticStateRequestManager(manager), request);

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(currentlyAssignedResolver.getId()))
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(currentlyAssignedResolver.getId()).remove(request.getId());
            if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(currentlyAssignedResolver.getId()).isEmpty())
            {
                manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(currentlyAssignedResolver.getId());
            }
        }

        currentlyAssignedResolver.onAssignedRequestCancelled(new WrappedStaticStateRequestManager(manager), request);

        manager.updateRequestState(request.getId(), RequestState.REPORTED);
        IToken<?> newAssignedResolverId = assignRequest(request, resolverTokenBlackList);

        return newAssignedResolverId;
    }

    /**
     * Method used to check if a given request token is assigned to a resolver.
     *
     * @param token   The request token to check for.
     * @return True when the request token has been assigned, false when not.
     */
    @Override
    public boolean isAssigned(final IToken<?> token)
    {
        return manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(token) != null;
    }

    @Override
    public void onRequestResolved(final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(token);
        @SuppressWarnings(RAWTYPES) final IRequestResolver resolver = manager.getResolverHandler().getResolverForRequest(token);

        //Retrieve a followup request.
        final List<IRequest<?>> followupRequests = resolver.getFollowupRequestForCompletion(manager, request);

        request.setState(manager, RequestState.FOLLOWUP_IN_PROGRESS);

        //Assign the followup to the parent as a child so that processing is still halted.
        if (followupRequests != null && !followupRequests.isEmpty())
        {
            followupRequests.forEach(followupRequest -> request.addChild(followupRequest.getId()));
            followupRequests.forEach(followupRequest -> followupRequest.setParent(request.getId()));
        }

        //Assign the followup request if need be
        if (followupRequests != null && !followupRequests.isEmpty() &&
              followupRequests.stream().anyMatch(followupRequest -> !isAssigned(followupRequest.getId())))
        {
            followupRequests.stream()
              .filter(followupRequest -> !isAssigned(followupRequest.getId()))
              .forEach(this::assignRequest);
        }

        //All follow ups resolved immediately or none where present.
        if (!request.hasChildren())
        {
            manager.updateRequestState(request.getId(), RequestState.COMPLETED);
        }
    }

    /**
     * Method used to handle the successful resolving of a request.
     *
     * @param token   The token of the request that got finished successfully.
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    public void onRequestCompleted(final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(token);

        request.getRequester().onRequestedRequestComplete(manager, request);

        //Check if the request has a parent, and if resolving of the parent is needed.
        if (request.hasParent())
        {
            @SuppressWarnings(RAWTYPES) final IRequest parentRequest = getRequest(request.getParent());

            manager.updateRequestState(request.getId(), RequestState.RECEIVED);
            parentRequest.removeChild(request.getId());

            request.setParent(null);

            if (!parentRequest.hasChildren())
            {
                //Normal processing still running, we received all dependencies, resolve parent request.
                if (parentRequest.getState() == RequestState.IN_PROGRESS)
                {
                    resolveRequest(parentRequest);
                }
                //Follow up processing is running, we completed all followups, complete the parent request.
                else if (parentRequest.getState() == RequestState.FOLLOWUP_IN_PROGRESS)
                {
                    manager.updateRequestState(parentRequest.getId(), RequestState.COMPLETED);
                }
            }
        }
    }

    /**
     * Method used to handle requests that were overruled or cancelled.
     * Cancels all children first, handles the creation of clean up requests.
     *
     * @param token   The token of the request that got cancelled or overruled
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    public void onRequestOverruled(final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(token);

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(token) == null)
        {
            manager.getRequestIdentitiesDataStore().getIdentities().remove(token);
            return;
        }

        //Lets cancel all our children first, else this would make a big fat mess.
        if (request.hasChildren())
        {
            final ImmutableCollection<IToken<?>> currentChildren = request.getChildren();
            currentChildren.forEach(this::onRequestCancelledDirectly);
        }

        final IRequestResolver<?> resolver = manager.getResolverHandler().getResolverForRequest(token);
        //Notify the resolver.
        resolver.onAssignedRequestBeingCancelled(manager, request);

        //This will notify everyone :D
        manager.updateRequestState(token, RequestState.COMPLETED);

        //Cancellation complete
        resolver.onAssignedRequestCancelled(manager, request);
    }

    /**
     * Method used to handle requests that were overruled or cancelled.
     * Cancels all children first, handles the creation of clean up requests.
     *
     * @param token   The token of the request that got cancelled or overruled
     */
    @Override
    @SuppressWarnings(UNCHECKED)
    public void onRequestCancelled(final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = manager.getRequestHandler().getRequest(token);

        if (request == null)
        {
            return;
        }

        if (request.hasParent())
        {
            this.onChildRequestCancelled(token);
        }
        else
        {
            this.onRequestCancelledDirectly(token);
        }

        manager.markDirty();
    }

    @Override
    public void onChildRequestCancelled(final IToken<?> token)
    {
        final IRequest<?> request = manager.getRequestForToken(token);
        final IRequest<?> parent = manager.getRequestForToken(request.getParent());
        parent.getChildren().forEach(this::onRequestCancelledDirectly);
        this.reassignRequest(parent, ImmutableList.of());
    }

    @Override
    public void onRequestCancelledDirectly(final IToken<?> token)
    {
        final IRequest<?> request = manager.getRequestForToken(token);
        if (request.hasChildren())
        {
            request.getChildren().forEach(this::onRequestCancelledDirectly);
        }

        processDirectCancellationAndNotifyRequesterOf(request);

        cleanRequestData(token);
    }

    @Override
    public void processDirectCancellationAndNotifyRequesterOf(final IRequest<?> request)
    {
        processDirectCancellationOf(request);
        request.getRequester().onRequestedRequestCancelled(manager, request);
    }

    @Override
    public void processDirectCancellationOf(final IRequest<?> request)
    {
        final IRequestResolver resolver = manager.getResolverForRequest(request.getId());
        resolver.onAssignedRequestBeingCancelled(new WrappedStaticStateRequestManager(manager), request);

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolver.getId()))
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).remove(request.getId());
            if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).isEmpty())
            {
                manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolver.getId());
            }
        }

        if (request.hasParent())
        {
            getRequest(request.getParent()).removeChild(request.getId());
        }
        request.setParent(null);
        request.setState(manager, RequestState.CANCELLED);

        resolver.onAssignedRequestCancelled(new WrappedStaticStateRequestManager(manager), request);
    }

    /**
     * Method used to resolve a request.
     * When this method is called the given request has to be assigned.
     *
     * @param request The request about to be resolved.
     * @throws IllegalArgumentException when the request is unknown, not resolved, or cannot be resolved.
     */
    @Override
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    public void resolveRequest(final IRequest request)
    {
        getRequest(request.getId());
        if (!isAssigned(request.getId()))
        {
            throw new IllegalArgumentException("The given request is not resolved");
        }

        if (request.getState() != RequestState.IN_PROGRESS)
        {
            throw new IllegalArgumentException("The given request is not in the right state. Required: " + RequestState.IN_PROGRESS + " - Found:" + request.getState());
        }

        if (request.hasChildren())
        {
            throw new IllegalArgumentException("Cannot resolve request with open Children");
        }

        final IRequestResolver resolver = manager.getResolverHandler().getResolverForRequest(request);

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
        resolver.resolveRequest(manager, request);
    }

    /**
     * Method called when the given manager gets notified of the receiving of a given task by its requester.
     * All communication with the resolver should be aborted by this time, so overrullings and cancelations need to be processed,
     * before this method is called.
     *
     * @param token   The token of the request.
     * @throws IllegalArgumentException Thrown when the token is unknown.
     */
    @Override
    public void cleanRequestData(final IToken<?> token)
    {
        manager.getLogger().debug("Removing " + token + " from the Manager as it has been completed and its package has been received by the requester.");
        getRequest(token);

        if (isAssigned(token))
        {
            final IRequestResolver<?> resolver = manager.getResolverHandler().getResolverForRequest(token);
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).remove(token);
            if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).isEmpty())
            {
                manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolver.getId());
            }
        }

        manager.getRequestIdentitiesDataStore().getIdentities().remove(token);
    }

    /**
     * Method used to get a registered request from a given token.
     *
     * @param token The token to query
     * @throws IllegalArgumentException when the token is unknown to the given manager.
     */
    @Override
    @SuppressWarnings(RAWTYPES)
    public IRequest getRequest(final IToken<?> token)
    {
        if (!manager.getRequestIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The given token is not registered as a request to this manager");
        }

        return getRequestOrNull(token);
    }

    /**
     * Method used to get a registered request fora given token.
     *
     * @param token The token to get the request for.
     * @return The request or null when no request with that token exists.
     */
    @Override
    @SuppressWarnings(RAWTYPES)
    public IRequest getRequestOrNull(final IToken<?> token)
    {
        manager.getLogger().debug("Retrieving the request for: " + token);

        return manager.getRequestIdentitiesDataStore().getIdentities().get(token);
    }

    /**
     * Returns all requests made by a given requester.
     *
     * @param requester The requester in question.
     *
     * @return A collection with request instances that are made by the given requester.
     */
    @Override
    public Collection<IRequest<?>> getRequestsMadeByRequester(final IRequester requester)
    {
        return manager.getRequestIdentitiesDataStore()
          .getIdentities()
          .values()
          .stream()
          .filter(iRequest -> iRequest.getRequester().getId().equals(requester.getId()))
          .collect(Collectors.toList());
    }
}
