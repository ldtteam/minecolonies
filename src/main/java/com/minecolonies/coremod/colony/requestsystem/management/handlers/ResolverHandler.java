package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;

/**
 * Class used to handle the inner workings of the request system with regards to resolvers.
 */
public final class ResolverHandler
{

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager   The manager to register the resolvers to.
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     *
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    public static Collection<IToken<?>> registerResolvers(final IStandardRequestManager manager, final IRequestResolver<?>... resolvers)
    {
        return Arrays.stream(resolvers).map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
    }

    /**
     * Method used to register a resolver.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track which resolver are registered.
     * </p>
     *
     * @param manager  The manager to register the resolver to.
     * @param resolver The resolver to register
     * @return The token of the newly registered resolver
     *
     * @throws IllegalArgumentException is thrown when either the token attached to the resolver is already registered or the resolver is already registered with a different
     *                                  token
     */
    public static IToken<?> registerResolver(final IStandardRequestManager manager, final IRequestResolver<? extends IRequestable> resolver)
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

        @SuppressWarnings(RAWTYPES) final Set<TypeToken> resolverTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        resolverTypes.remove(TypeConstants.OBJECT);
        resolverTypes.forEach(c -> {
            if (!manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().containsKey(c))
            {
                manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().put(c, new ArrayList<>());
            }

            LogHandler.log("Registering resolver: " + resolver + " with request type: " + c);
            manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().get(c).add(resolver.getId());
        });

        return resolver.getId();
    }

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager   The manager to register the resolvers to.
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     *
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    public static Collection<IToken<?>> registerResolvers(final IStandardRequestManager manager, final Collection<IRequestResolver<?>> resolvers)
    {
        return resolvers.stream().map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
    }

    /**
     * Method used to remove a registered resolver.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager The manager to remove the resolver from.
     * @param token   The token of the resolver to remove.
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    public static void removeResolver(final IStandardRequestManager manager, final IToken<?> token)
    {
        if (!manager.getRequestResolverIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The token is unknown to this manager.");
        }

        removeResolver(manager, getResolver(manager, token));
    }

    /**
     * Method used to remove a registered resolver.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager  The manager to remove the resolver from.
     * @param resolver The resolver to remove
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    public static void removeResolver(final IStandardRequestManager manager, final IRequestResolver<?> resolver)
    {
        final IRequestResolver<?> registeredResolver = getResolver(manager, resolver.getId());

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

        removeResolverInternal(manager, resolver);
    }

    /**
     * Method to get all requests currently assigned to a resolver.
     *
     * @param manager The manager to get the requests from.
     * @param resolver The resolver to get the requests from.
     *
     * @return A collection with requests tokens.
     */
    public static Collection<IToken<?>> getRequestsAssignedToResolver(final IStandardRequestManager manager, final IRequestResolver<?> resolver)
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
     * <p>
     * Is only used internally.
     * Querries the resolverBiMap to get the resolver for a given Token.
     * </p>
     *
     * @param manager The manager to retrieve the resolver from.
     * @param token   The token of the resolver to look up.
     * @return The resolver registered with the given token.
     *
     * @throws IllegalArgumentException is thrown when the given token is not registered to any IRequestResolver
     */
    public static IRequestResolver<? extends IRequestable> getResolver(final IStandardRequestManager manager, final IToken<?> token)
    {
        if (!manager.getRequestResolverIdentitiesDataStore().getIdentities().containsKey(token))
        {
            throw new IllegalArgumentException("The given token for a resolver is not known to this manager!");
        }

        LogHandler.log("Retrieving resolver for: " + token);

        return manager.getRequestResolverIdentitiesDataStore().getIdentities().get(token);
    }

    public static void removeResolverInternal(final IStandardRequestManager manager, final IRequestResolver<?> resolver)
    {
        manager.getRequestResolverIdentitiesDataStore().getIdentities().remove(resolver.getId());
        @SuppressWarnings(RAWTYPES) final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        requestTypes.remove(TypeConstants.OBJECT);
        requestTypes.forEach(c -> {
            LogHandler.log("Removing resolver: " + resolver + " with request type: " + c);
            manager.getRequestableTypeRequestResolverAssignmentDataStore().getAssignments().get(c).remove(resolver.getId());
        });
    }

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager   The manager to remove the resolver from.
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    public static void removeResolvers(final IStandardRequestManager manager, final IRequestResolver<?>... resolvers)
    {
        removeResolvers(manager, Arrays.asList(resolvers));
    }

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param manager   The manager to remove the resolver from.
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    public static void removeResolvers(final IStandardRequestManager manager, final Iterable<IRequestResolver<?>> resolvers)
    {
        resolvers.forEach((IRequestResolver<?> resolver) ->
                            removeResolver(manager, resolver));
    }

    /**
     * Method used to add a request to a resolver.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param manager  The manager to modify
     * @param resolver The resolver to add the request to.
     * @param request  The request to add to the resolver.
     */
    public static void addRequestToResolver(final IStandardRequestManager manager, final IRequestResolver<?> resolver, final IRequest<?> request)
    {
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolver.getId()))
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().put(resolver.getId(), new HashSet<>());
        }

        LogHandler.log("Adding request: " + request + " to resolver: " + resolver);

        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).add(request.getId());

        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.ASSIGNED);
    }

    /**
     * Method used to remove a request from a resolver.
     * <p>
     * <p>
     * Is only used internally.
     * The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param manager  The manager to modify
     * @param resolver The resolver to remove the given request from.
     * @param request  The request to remove.
     * @throws IllegalArgumentException is thrown when the resolver is unknown, or when the given request is not registered to the given resolver.
     */
    public static void removeRequestFromResolver(final IStandardRequestManager manager, final IRequestResolver<?> resolver, final IRequest<?> request)
    {
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolver.getId()))
        {
            throw new IllegalArgumentException("The given resolver is unknown to this Manager");
        }

        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).contains(request.getId()))
        {
            throw new IllegalArgumentException("The given request is not registered to the given resolver.");
        }

        LogHandler.log("Removing request: " + request + " from resolver: " + resolver);

        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).remove(request.getId());
        if (manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolver.getId()).isEmpty())
        {
            manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolver.getId());
        }
    }

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param manager      The manager to get the resolver from.
     * @param requestToken The token of a request a the assigned resolver is requested for.
     * @return The resolver for the request with the given token.
     *
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    public static IRequestResolver<? extends IRequestable> getResolverForRequest(final IStandardRequestManager manager, final IToken<?> requestToken)
    {
        RequestHandler.getRequest(manager, requestToken);

        @Nullable
        final IToken<?> resolverToken = manager.getRequestResolverRequestAssignmentDataStore().getAssignmentForValue(requestToken);
        if (resolverToken == null)
        {
            throw new IllegalArgumentException("The given request: " + requestToken + " is not resolved.");
        }

        return manager.getRequestResolverIdentitiesDataStore().getIdentities().get(resolverToken);
    }

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param manager The manager to get the resolver from.
     * @param request The request a the assigned resolver is requested for.
     * @return The resolver for the request.
     *
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    public static IRequestResolver<? extends IRequestable> getResolverForRequest(final IStandardRequestManager manager, final IRequest<?> request)
    {
        return getResolverForRequest(manager, (IToken<?>) request.getId());
    }

    /**
     * Method used to reassign requests based on a predicate
     *
     * @param manager The manager to update reassign requests on
     * @param shouldTriggerReassign the predicate to determine whether a request should be reassigned
     */
    public static void onColonyUpdate(final IStandardRequestManager manager, final Predicate<IRequest> shouldTriggerReassign)
    {
        manager.getRequestResolverIdentitiesDataStore().getIdentities().values().forEach(resolver -> resolver.onColonyUpdate(manager, shouldTriggerReassign));
    }
}
