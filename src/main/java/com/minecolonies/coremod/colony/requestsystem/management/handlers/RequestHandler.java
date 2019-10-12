package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.manager.AssigningStrategy;
import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedBlacklistAssignmentRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Class used to handle the inner workings of the request system with regards to requests.
 */
public final class RequestHandler
{

    @SuppressWarnings(UNCHECKED)
    public static <Request extends IRequestable> IRequest<Request> createRequest(final IStandardRequestManager manager, final IRequester requester, final Request request)
    {
        final IToken<UUID> token = TokenHandler.generateNewToken(manager);

        final IRequest<Request> constructedRequest = manager.getFactoryController()
                                                       .getNewInstance(TypeToken.of((Class<? extends IRequest<Request>>) RequestMappingHandler.getRequestableMappings()
                                                                                                                           .get(request.getClass())), request, token, requester);

        LogHandler.log("Creating request for: " + request + ", token: " + token + " and output: " + constructedRequest);

        registerRequest(manager, constructedRequest);

        return constructedRequest;
    }

    public static void registerRequest(final IStandardRequestManager manager, final IRequest<?> request)
    {
        if (manager.getRequestIdentitiesDataStore().getIdentities().containsKey(request.getId()) ||
              manager.getRequestIdentitiesDataStore().getIdentities().containsValue(request))
        {
            throw new IllegalArgumentException("The given request is already known to this manager");
        }

        LogHandler.log("Registering request: " + request);

        manager.getRequestIdentitiesDataStore().getIdentities().put(request.getId(), request);
    }

    /**
     * Method used to assign a given request to a resolver. Does not take any blacklist into account.
     *
     * @param manager The manager to modify.
     * @param request The request to assign
     * @throws IllegalArgumentException when the request is already assigned
     */
    @SuppressWarnings(UNCHECKED)
    public static void assignRequest(final IStandardRequestManager manager, final IRequest<?> request)
    {
        assignRequest(manager, request, Collections.emptyList());
    }

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
     *
     * @param manager                The manager to modify.
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @SuppressWarnings(UNCHECKED)
    public static IToken<?> assignRequest(final IStandardRequestManager manager, final IRequest<?> request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        switch (request.getStrategy())
        {
            case PRIORITY_BASED:
                return assignRequestDefault(manager, request, resolverTokenBlackList);
            case FASTEST_FIRST:
            {
                MineColonies.getLogger().warn("Fastest First strategy not implemented yet.");
                return assignRequestDefault(manager, request, resolverTokenBlackList);
            }
        }

        return null;
    }

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
     * Uses the default assigning strategy: {@link AssigningStrategy#PRIORITY_BASED}
     *
     * @param manager                The manager to modify.
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    public static IToken<?> assignRequestDefault(final IStandardRequestManager manager, final IRequest request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        //Check if the request is registered
        getRequest(manager, request.getId());

        LogHandler.log("Starting resolver assignment search for request: " + request);

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
                                                                        .map(iToken -> ResolverHandler.getResolver(manager, iToken)))
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
            if (!resolver.canResolve(manager, request))
            {
                continue;
            }

            @Nullable final List<IToken<?>> attemptResult = resolver.attemptResolve(new WrappedBlacklistAssignmentRequestManager(manager, resolverTokenBlackList), request);

            //Skip if attempt failed (aka attemptResult == null)
            if (attemptResult == null)
            {
                continue;
            }

            //Successfully found a resolver. Registering
            LogHandler.log("Finished resolver assignment search for request: " + request + " successfully");

            ResolverHandler.addRequestToResolver(manager, resolver, request);
            //TODO: Change this false to simulation.
            resolver.onAssignedToThisResolver(manager, request, false);

            for (final IToken<?> childRequestToken :
              attemptResult)
            {
                @SuppressWarnings(RAWTYPES) final IRequest childRequest = RequestHandler.getRequest(manager, childRequestToken);

                childRequest.setParent(request.getId());
                request.addChild(childRequest.getId());

                if (!isAssigned(manager, childRequestToken))
                {
                    assignRequest(manager, childRequest, resolverTokenBlackList);
                }
            }

            if (request.getState().ordinal() < RequestState.IN_PROGRESS.ordinal())
            {
                request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
                if (!request.hasChildren())
                {
                    resolveRequest(manager, request);
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
     * @param manager                The manager that is reassigning a request.
     * @param request                The request that is being reassigned.
     * @param resolverTokenBlackList The blacklist to which not to assign the request.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     *
     * @throws IllegalArgumentException Thrown when something went wrong.
     */
    public static IToken<?> reassignRequest(final IStandardRequestManager manager, final IRequest<?> request, final Collection<IToken<?>> resolverTokenBlackList)
    {
        //Get the current resolver
        IRequestResolver currentResolver = null;
        if (RequestHandler.isAssigned(manager, request.getId()))
        {
            currentResolver = ResolverHandler.getResolverForRequest(manager, request);
        }

        IToken<?> parent = null;
        if (request.hasParent())
        {
            parent = request.getParent();
        }

        //Cancel the request to restart the search
        processInternalCancellation(manager, request.getId());

        if (currentResolver != null)
        {
            if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(currentResolver.getId()))
            {
                manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(currentResolver.getId()).remove(request.getId());
                if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(currentResolver.getId()).isEmpty())
                {
                    manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(currentResolver.getId());
                }
            }
        }

        manager.updateRequestState(request.getId(), RequestState.REPORTED);
        IToken<?> resolver = assignRequest(manager, request, resolverTokenBlackList);

        if (parent != null)
        {
            request.setParent(parent);
            final IRequest parentRequest = RequestHandler.getRequest(manager, parent);
            parentRequest.addChild(request.getId());
        }

        return resolver;
    }

    /**
     * Method used to check if a given request token is assigned to a resolver.
     *
     * @param manager The manager to check for.
     * @param token   The request token to check for.
     * @return True when the request token has been assigned, false when not.
     */
    public static boolean isAssigned(final IStandardRequestManager manager, final IToken<?> token)
    {
        return manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(token) != null;
    }

    /**
     * Method used to handle the successful resolving of a request.
     *
     * @param manager The manager that got notified of the successful resolving of the request.
     * @param token   The token of the request that got finished successfully.
     */
    @SuppressWarnings(UNCHECKED)
    public static void onRequestSuccessful(final IStandardRequestManager manager, final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(manager, token);
        @SuppressWarnings(RAWTYPES) final IRequestResolver resolver = ResolverHandler.getResolverForRequest(manager, token);

        request.getRequester().onRequestComplete(manager, token);

        //Retrieve a followup request.
        final List<IRequest<?>> followupRequests = resolver.getFollowupRequestForCompletion(manager, request);

        //Check if the request has a parent
        if (request.hasParent())
        {
            @SuppressWarnings(RAWTYPES) final IRequest parentRequest = getRequest(manager, request.getParent());

            //Assign the followup to the parent as a child so that processing is still halted.
            if (followupRequests != null && !followupRequests.isEmpty())
            {
                followupRequests.forEach(followupRequest -> parentRequest.addChild(followupRequest.getId()));
                followupRequests.forEach(followupRequest -> followupRequest.setParent(parentRequest.getId()));
            }

            manager.updateRequestState(request.getId(), RequestState.RECEIVED);
            parentRequest.removeChild(request.getId());

            request.setParent(null);

            if (!parentRequest.hasChildren() && parentRequest.getState() == RequestState.IN_PROGRESS)
            {
                resolveRequest(manager, parentRequest);
            }
        }

        //Assign the followup request if need be
        if (followupRequests != null && !followupRequests.isEmpty() &&
              followupRequests.stream().anyMatch(followupRequest -> !isAssigned(manager, followupRequest.getId())))
        {
            followupRequests.stream()
              .filter(followupRequest -> !isAssigned(manager, followupRequest.getId()))
              .forEach(unassignedFollowupRequest -> assignRequest(manager, unassignedFollowupRequest));
        }
    }

    /**
     * Method used to handle requests that were overruled or cancelled.
     * Cancels all children first, handles the creation of clean up requests.
     *
     * @param manager The manager that got notified of the cancellation or overruling.
     * @param token   The token of the request that got cancelled or overruled
     */
    @SuppressWarnings(UNCHECKED)
    public static void onRequestOverruled(final IStandardRequestManager manager, final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(manager, token);

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(token) == null)
        {
            manager.getRequestIdentitiesDataStore().getIdentities().remove(token);
            return;
        }

        //Lets cancel all our children first, else this would make a big fat mess.
        if (request.hasChildren())
        {
            final ImmutableCollection<IToken<?>> currentChildren = request.getChildren();
            currentChildren.forEach(t -> onRequestCancelled(manager, t));
        }

        //Notify the resolver.
        ResolverHandler.getResolverForRequest(manager, token).onRequestBeingOverruled(manager, request);

        //This will notify everyone :D
        manager.updateRequestState(token, RequestState.COMPLETED);
    }

    /**
     * Method used to handle requests that were overruled or cancelled.
     * Cancels all children first, handles the creation of clean up requests.
     *
     * @param manager The manager that got notified of the cancellation or overruling.
     * @param token   The token of the request that got cancelled or overruled
     */
    @SuppressWarnings(UNCHECKED)
    public static void onRequestCancelled(final IStandardRequestManager manager, final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = RequestHandler.getRequest(manager, token);

        if (request == null)
        {
            return;
        }

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.CANCELLED);

        processInternalCancellation(manager, token);

        //Notify the requester.
        final IRequester requester = request.getRequester();
        requester.onRequestCancelled(manager, token);

        cleanRequestData(manager, token);
    }

    /**
     * Method used to handle cancellation internally without notifying the requester that the request has been cancelled.
     *
     * @param manager The manager for which the cancellation is internally processed.
     * @param token   The token which is internally processed.
     */
    @SuppressWarnings(UNCHECKED)
    public static void processInternalCancellation(final IStandardRequestManager manager, final IToken<?> token)
    {
        @SuppressWarnings(RAWTYPES) final IRequest request = getRequest(manager, token);

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(token) == null)
        {
            return;
        }

        //Lets cancel all our children first, else this would make a big fat mess.
        if (request.hasChildren())
        {
            final ImmutableCollection<IToken<?>> currentChildren = request.getChildren();
            currentChildren.forEach(t -> onRequestCancelled(manager, t));
        }

        //Now lets get ourselfs a clean up.
        final IRequestResolver<?> targetResolver = ResolverHandler.getResolverForRequest(manager, request);
        processParentReplacement(manager, request, targetResolver.onRequestCancelled(manager, request));

        manager.updateRequestState(token, RequestState.FINALIZING);
    }

    /**
     * Method used during clean up to process Parent replacement.
     *
     * @param manager   The manager which is handling the cleanup.
     * @param target    The target request, which gets their parent replaced.
     * @param newParent The new cleanup request used to cleanup the target when it is finished.
     */
    @SuppressWarnings({RAWTYPES, UNCHECKED})
    public static void processParentReplacement(final IStandardRequestManager manager, final IRequest target, final IRequest newParent)
    {
        //Clear out the existing parent.
        if (target.hasParent())
        {
            final IRequest currentParent = RequestHandler.getRequest(manager, target.getParent());

            currentParent.removeChild(target.getId());
            target.setParent(null);
        }

        if (newParent != null)
        {
            //Switch out the parent, and add the old child to the cleanup request as new child
            newParent.addChild(target.getId());
            target.setParent(newParent.getId());

            //Assign the new parent request if it is not assigned yet.
            if (!RequestHandler.isAssigned(manager, newParent.getId()))
            {
                RequestHandler.assignRequest(manager, newParent);
            }
        }
    }

    /**
     * Method used to resolve a request.
     * When this method is called the given request has to be assigned.
     *
     * @param manager The manager requesting the resolving.
     * @param request The request about to be resolved.
     * @throws IllegalArgumentException when the request is unknown, not resolved, or cannot be resolved.
     */
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    public static void resolveRequest(final IStandardRequestManager manager, final IRequest request)
    {
        getRequest(manager, request.getId());
        if (!isAssigned(manager, request.getId()))
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

        final IRequestResolver resolver = ResolverHandler.getResolverForRequest(manager, request);

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
        resolver.resolve(manager, request);
    }

    /**
     * Method called when the given manager gets notified of the receiving of a given task by its requester.
     * All communication with the resolver should be aborted by this time, so overrullings and cancelations need to be processed,
     * before this method is called.
     *
     * @param manager The manager that got notified.
     * @param token   The token of the request.
     * @throws IllegalArgumentException Thrown when the token is unknown.
     */
    public static void cleanRequestData(final IStandardRequestManager manager, final IToken<?> token)
    {
        LogHandler.log("Removing " + token + " from the Manager as it has been completed and its package has been received by the requester.");
        getRequest(manager, token);

        if (isAssigned(manager, token))
        {
            final IRequestResolver<?> resolver = ResolverHandler.getResolverForRequest(manager, token);
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
    @SuppressWarnings(RAWTYPES)
    public static IRequest getRequest(final IStandardRequestManager manager, final IToken<?> token)
    {
        if (!manager.getRequestIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The given token is not registered as a request to this manager");
        }

        return getRequestOrNull(manager, token);
    }

    /**
     * Method used to get a registered request fora given token.
     *
     * @param token The token to get the request for.
     * @return The request or null when no request with that token exists.
     */
    @SuppressWarnings(RAWTYPES)
    public static IRequest getRequestOrNull(final IStandardRequestManager manager, final IToken<?> token)
    {
        LogHandler.log("Retrieving the request for: " + token);

        return manager.getRequestIdentitiesDataStore().getIdentities().get(token);
    }

    /**
     * Returns all requests made by a given requester.
     *
     * @param manager THe manager.
     * @param requester The requester in question.
     *
     * @return A collection with request instances that are made by the given requester.
     */
    public static Collection<IRequest<?>> getRequestsMadeByRequester(final IStandardRequestManager manager, final IRequester requester)
    {
        return manager.getRequestIdentitiesDataStore()
          .getIdentities()
          .values()
          .stream()
          .filter(iRequest -> iRequest.getRequester().getId().equals(requester.getId()))
          .collect(Collectors.toList());
    }

    /**
     * Wrapper for a assignment result.
     */
    private static final class AssigningResult<T> implements Comparable<AssigningResult<T>>
    {
        @SuppressWarnings(RAWTYPES)
        private final IRequestResolver resolver;
        private final List<IToken<T>>  children;

        @SuppressWarnings(RAWTYPES)
        private AssigningResult(final IRequestResolver resolver, final List<IToken<T>> children)
        {
            this.resolver = resolver;
            this.children = new ArrayList<>(children);
        }

        /*
        public List<IToken<T>> getChildren()
        {
            return Collections.unmodifiableList(children);
        }
        */

        @Override
        public int compareTo(@NotNull final AssigningResult<T> o)
        {
            return this.children.size() != o.children.size() ? this.children.size() - o.children.size() : o.getResolver().getPriority() - this.getResolver().getPriority();
        }

        @SuppressWarnings(RAWTYPES)
        public IRequestResolver getResolver()
        {
            return resolver;
        }
    }
}
