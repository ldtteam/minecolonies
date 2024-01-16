package com.minecolonies.core.colony.requestsystem.management.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.management.IResolverHandler;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.core.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class used to handle the inner workings of the request system with regards to resolvers.
 */
public class ResolverHandler implements IResolverHandler
{

    private final IStandardRequestManager manager;

    /**
     * Additional temporary blacklist of the request handler.
     */
    private List<IToken<?>> tempBlackList = new ArrayList<>();

    public ResolverHandler(final IStandardRequestManager manager)
    {
        this.manager = manager;
    }

    @Override
    public IRequestManager getManager()
    {
        return manager;
    }

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    @Override
    public Collection<IToken<?>> registerResolvers(final IRequestResolver<?>... resolvers)
    {
        return Arrays.stream(resolvers).map(this::registerResolver).collect(Collectors.toList());
    }

    /**
     * Method used to register a resolver.
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track which resolver are registered.
     * </p>
     *
     * @param resolver The resolver to register
     * @return The token of the newly registered resolver
     * @throws IllegalArgumentException is thrown when either the token attached to the resolver is already registered or the resolver is already registered with a different token
     */
    @Override
    public IToken<?> registerResolver(final IRequestResolver<? extends IRequestable> resolver)
    {
        if (manager.getRequestResolverIdentitiesDataStore().getIdentities().containsKey(resolver.getId()))
        {
            throw new IllegalArgumentException("The token attached to this resolver is already registered. Cannot register twice!");
        }

        if (manager.getRequestResolverIdentitiesDataStore().getIdentities().containsValue(resolver))
        {
            throw new IllegalArgumentException("The given resolver is already registered with a different token. Cannot register twice!");
        }

        manager.getRequestResolverIdentitiesDataStore().getIdentities().put(resolver.getId(), resolver);

        final Set<TypeToken<?>> resolverTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        resolverTypes.remove(TypeConstants.OBJECT);
        resolverTypes.forEach(c -> {
            if (!manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().containsKey(c))
            {
                manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().put(c, new ArrayList<>());
            }

            manager.log("Registering resolver: " + resolver + " with request type: " + c);
            manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().get(c).add(resolver.getId());
        });

        return resolver.getId();
    }

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    @Override
    public Collection<IToken<?>> registerResolvers(final Collection<IRequestResolver<?>> resolvers)
    {
        return resolvers.stream().map(this::registerResolver).collect(Collectors.toList());
    }

    /**
     * Method used to remove a registered resolver.
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param token The token of the resolver to remove.
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    @Override
    public void removeResolver(final IToken<?> token)
    {
        if (!manager.getRequestResolverIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The token is unknown to this manager.");
        }

        removeResolver(getResolver(token));
    }

    /**
     * Method used to remove a registered resolver.
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolver The resolver to remove
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    @Override
    public void removeResolver(final IRequestResolver<?> resolver)
    {
        final IRequestResolver<?> registeredResolver = getResolver(resolver.getId());

        if (!registeredResolver.equals(resolver))
        {
            throw new IllegalArgumentException("The given resolver and the resolver that is registered with its token are not the same.");
        }

        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments()
              .containsKey(registeredResolver.getId())
              && !manager.getRequestResolverRequestAssignmentDataStore().getAssignments()
                    .get(registeredResolver.getId()).isEmpty())
        {
            throw new IllegalArgumentException("Cannot remove a resolver that is still in use. Reassign all registered requests before removing");
        }

        removeResolverInternal(resolver);
    }

    /**
     * Internal method that handles the removal of a single resolvers that is attached to a provider that is being removed.
     *
     * @param assignedResolvers The list of resolvers which are being removed.
     * @param resolverToken     The id of the resolver which is being removed, needs to be part of the assignedResolvers list.
     */
    @Override
    public void processResolverForRemoval(final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
    {
        //Make sure that the resolver is actually supposed to be deleted.
        Validate.isTrue(assignedResolvers.contains(resolverToken));

        //Skip if the resolver has no requests assigned.
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolverToken)
              || manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken).isEmpty())
        {
            //No requests assigned so lets process this resolver as such.
            removeResolverWithoutAssignedRequests(resolverToken);
            return;
        }

        //This resolver has currently requests assigned, which needs to be handled separately
        removeResolverWithAssignedRequests(assignedResolvers, resolverToken);
    }

    /**
     * Internal method that is handling the removal of a resolver that has no requests assigned to it.
     *
     * @param resolverToken The resolver from the provider which is being removed, but has no requests assigned anymore.
     */
    @VisibleForTesting
    void removeResolverWithoutAssignedRequests(@NotNull final IToken<?> resolverToken)
    {
        manager.log("Removing resolver without assigned requests: " + resolverToken);
        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolverToken);

        manager.getResolverHandler().removeResolver(resolverToken);
    }

    /**
     * Internal method that is handling the removal of a resolver that has requests currently assigned to it. Reassigns the assigned requests, using the provided list as
     * blacklist.
     *
     * @param assignedResolvers The resolvers from the provider that is being removed.
     * @param resolverToken     The particular resolver that is being removed. Needs to be part of the assignedResolvers list.
     */
    @VisibleForTesting
    void removeResolverWithAssignedRequests(@NotNull final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
    {
        //Make sure that the resolver is actually supposed to be deleted
        Validate.isTrue(assignedResolvers.contains(resolverToken));

        //Clone the original list to modify it during iteration, if need be.
        Collection<IToken<?>> assignedRequests = new ArrayList<>(manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken));
        manager.log("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);
        tempBlackList.addAll(assignedResolvers);

        for (final IToken<?> requestToken : assignedRequests)
        {
            final IRequest<?> req = manager.getRequestForToken(requestToken);
            for (final IToken<?> childReq : req.getChildren())
            {
                manager.getRequestHandler().onRequestCancelledDirectly(childReq);
            }
        }

        assignedRequests = new ArrayList<>(manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken));
        //Get all assigned requests and reassign them.
        for (final IToken<?> requestToken : assignedRequests)
        {
            manager.reassignRequest(requestToken, assignedResolvers);
        }

        tempBlackList.removeAll(assignedResolvers);

        removeResolverWithoutAssignedRequests(resolverToken);

        manager.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
    }

    /**
     * Method to get all requests currently assigned to a resolver.
     *
     * @param resolver The resolver to get the requests from.
     * @return A collection with requests tokens.
     */
    @Override
    public Collection<IToken<?>> getRequestsAssignedToResolver(final IRequestResolver<?> resolver)
    {
        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments()
              .containsKey(resolver.getId()))
        {
            return manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId());
        }

        return Lists.newArrayList();
    }

    /**
     * Method to get a resolver from a given token.
     * <p>
     * Is only used internally. Querries the resolverBiMap to get the resolver for a given Token.
     * </p>
     *
     * @param token The token of the resolver to look up.
     * @return The resolver registered with the given token.
     * @throws IllegalArgumentException is thrown when the given token is not registered to any IRequestResolver
     */
    @Override
    public IRequestResolver<? extends IRequestable> getResolver(final IToken<?> token)
    {
        if (!manager.getRequestResolverIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The given token for a resolver is not known to this manager!");
        }

        manager.log("Retrieving resolver for: " + token);

        return manager.getRequestResolverIdentitiesDataStore().getIdentities().get(token);
    }

    @Override
    public void removeResolverInternal(final IRequestResolver<?> resolver)
    {
        manager.getRequestResolverIdentitiesDataStore().getIdentities().remove(resolver.getId());
        final Set<TypeToken<?>> requestTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        requestTypes.remove(TypeConstants.OBJECT);
        requestTypes.forEach(c -> {
            manager.log("Removing resolver: " + resolver + " with request type: " + c);
            manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().get(c).remove(resolver.getId());
        });
    }

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    @Override
    public void removeResolvers(final IRequestResolver<?>... resolvers)
    {
        removeResolvers(Arrays.asList(resolvers));
    }

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    @Override
    public void removeResolvers(final Iterable<IRequestResolver<?>> resolvers)
    {
        resolvers.forEach(this::removeResolver);
    }

    /**
     * Method used to add a request to a resolver.
     * <p>
     * Is only used internally. The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param resolver The resolver to add the request to.
     * @param request  The request to add to the resolver.
     */
    @Override
    public void addRequestToResolver(final IRequestResolver<?> resolver, final IRequest<?> request)
    {
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolver.getId()))
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().put(resolver.getId(), new HashSet<>());
        }

        manager.log("Adding request: " + request + " to resolver: " + resolver);

        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).add(request.getId());

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.ASSIGNED);
    }

    /**
     * Method used to remove a request from a resolver.
     * <p>
     * Is only used internally. The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param resolver The resolver to remove the given request from.
     * @param request  The request to remove.
     * @throws IllegalArgumentException is thrown when the resolver is unknown, or when the given request is not registered to the given resolver.
     */
    @Override
    public void removeRequestFromResolver(final IRequestResolver<?> resolver, final IRequest<?> request)
    {
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolver.getId()))
        {
            throw new IllegalArgumentException("The given resolver is unknown to this Manager");
        }

        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).contains(request.getId()))
        {
            throw new IllegalArgumentException("The given request is not registered to the given resolver.");
        }

        manager.log("Removing request: " + request + " from resolver: " + resolver);

        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).remove(request.getId());
        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).isEmpty())
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolver.getId());
        }
    }

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param requestToken The token of a request a the assigned resolver is requested for.
     * @return The resolver for the request with the given token.
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    @Override
    public IRequestResolver<? extends IRequestable> getResolverForRequest(final IToken<?> requestToken)
    {
        manager.getRequestHandler().getRequest(requestToken);

        @Nullable final IToken<?> resolverToken = manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(requestToken);
        if (resolverToken == null)
        {
            throw new IllegalArgumentException("The given request: " + requestToken + " is not resolved.");
        }

        return manager.getRequestResolverIdentitiesDataStore().getIdentities().get(resolverToken);
    }

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param request The request a the assigned resolver is requested for.
     * @return The resolver for the request.
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    @Override
    public IRequestResolver<? extends IRequestable> getResolverForRequest(final IRequest<?> request)
    {
        return getResolverForRequest((IToken<?>) request.getId());
    }

    /**
     * Method used to reassign requests based on a predicate
     *
     * @param shouldTriggerReassign the predicate to determine whether a request should be reassigned
     */
    @Override
    public void onColonyUpdate(final Predicate<IRequest<?>> shouldTriggerReassign)
    {
        manager.getRequestResolverIdentitiesDataStore().getIdentities().values().forEach(resolver -> resolver.onColonyUpdate(manager, shouldTriggerReassign));
    }

    @Override
    public boolean isBeingRemoved(final IToken<?> id)
    {
        return tempBlackList.contains(id);
    }
}
