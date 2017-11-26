package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;

import java.util.*;
import java.util.stream.Collectors;

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
    public static Collection<IToken> registerResolvers(final IStandardRequestManager manager, final IRequestResolver... resolvers) throws IllegalArgumentException
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
    public static IToken registerResolver(final IStandardRequestManager manager, final IRequestResolver resolver) throws IllegalArgumentException
    {
        if (manager.getResolverBiMap().containsKey(resolver.getRequesterId()))
        {
            throw new IllegalArgumentException("The token attached to this resolver is already registered. Cannot register twice!");
        }

        if (manager.getResolverBiMap().containsValue(resolver))
        {
            throw new IllegalArgumentException("The given resolver is already registered with a different token. Cannot register twice!");
        }

        manager.getResolverBiMap().put(resolver.getRequesterId(), resolver);

        Set<TypeToken> resolverTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        resolverTypes.remove(TypeConstants.OBJECT);
        resolverTypes.forEach(c -> {
            if (!manager.getRequestClassResolverMap().containsKey(c))
            {
                manager.getRequestClassResolverMap().put(c, new ArrayList<>());
            }

            LogHandler.log("Registering resolver: " + resolver + " with request type: " + c);
            manager.getRequestClassResolverMap().get(c).add(resolver);
        });

        return resolver.getRequesterId();
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
    public static Collection<IToken> registerResolvers(final IStandardRequestManager manager, final Collection<IRequestResolver> resolvers)
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
    public static void removeResolver(final IStandardRequestManager manager, final IToken token) throws IllegalArgumentException
    {
        if (!manager.getResolverBiMap().containsKey(token))
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
    public static void removeResolver(final IStandardRequestManager manager, final IRequestResolver resolver) throws IllegalArgumentException
    {
        final IRequestResolver registeredResolver = getResolver(manager, resolver.getRequesterId());

        if (!registeredResolver.equals(resolver))
        {
            throw new IllegalArgumentException("The given resolver and the resolver that is registered with its token are not the same.");
        }

        if (manager.getResolverRequestMap().containsKey(registeredResolver.getRequesterId()) && manager.getResolverRequestMap().get(registeredResolver.getRequesterId()).size() > 0)
        {
            throw new IllegalArgumentException("Cannot remove a resolver that is still in use. Reassign all registered requests before removing");
        }


        removeResolverInternal(manager, resolver);
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
    public static IRequestResolver getResolver(final IStandardRequestManager manager, final IToken token) throws IllegalArgumentException
    {
        if (!manager.getResolverBiMap().containsKey(token))
        {
            throw new IllegalArgumentException("The given token for a resolver is not known to this manager!");
        }

        LogHandler.log("Retrieving resolver for: " + token);

        return manager.getResolverBiMap().get(token);
    }

    public static void removeResolverInternal(final IStandardRequestManager manager, final IRequestResolver resolver)
    {
        manager.getResolverBiMap().remove(resolver.getRequesterId());
        Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
        requestTypes.remove(TypeConstants.OBJECT);
        requestTypes.forEach(c -> {
            LogHandler.log("Removing resolver: " + resolver + " with request type: " + c);
            manager.getRequestClassResolverMap().get(c).remove(resolver);
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
    public static void removeResolvers(final IStandardRequestManager manager, final IRequestResolver... resolvers)
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
    public static void removeResolvers(final IStandardRequestManager manager, final Iterable<IRequestResolver> resolvers)
    {
        resolvers.forEach((IRequestResolver resolver) ->
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
    public static void addRequestToResolver(final IStandardRequestManager manager, final IRequestResolver resolver, final IRequest request)
    {
        if (!manager.getResolverRequestMap().containsKey(resolver.getRequesterId()))
        {
            manager.getResolverRequestMap().put(resolver.getRequesterId(), new HashSet<>());
        }

        LogHandler.log("Adding request: " + request + " to resolver: " + resolver);

        manager.getResolverRequestMap().get(resolver.getRequesterId()).add(request.getToken());
        manager.getRequestResolverMap().put(request.getToken(), resolver.getRequesterId());

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
    public static void removeRequestFromResolver(final IStandardRequestManager manager, final IRequestResolver resolver, final IRequest request) throws IllegalArgumentException
    {
        if (!manager.getResolverRequestMap().containsKey(resolver.getRequesterId()))
        {
            throw new IllegalArgumentException("The given resolver is unknown to this Manager");
        }

        if (!manager.getResolverRequestMap().get(resolver.getRequesterId()).contains(request.getToken()))
        {
            throw new IllegalArgumentException("The given request is not registered to the given resolver.");
        }

        LogHandler.log("Removing request: " + request + " from resolver: " + resolver);

        manager.getResolverRequestMap().get(resolver.getRequesterId()).remove(request.getToken());
        manager.getRequestResolverMap().remove(request.getToken());
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
    public static IRequestResolver getResolverForRequest(final IStandardRequestManager manager, final IToken requestToken) throws IllegalArgumentException
    {
        RequestHandler.getRequest(manager, requestToken);

        if (!manager.getRequestResolverMap().containsKey(requestToken))
        {
            throw new IllegalArgumentException("The given token belongs to a not resolved request");
        }

        return getResolver(manager, manager.getRequestResolverMap().get(requestToken));
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
    public static IRequestResolver getResolverForRequest(final IStandardRequestManager manager, final IRequest request)
    {
        RequestHandler.getRequest(manager, request.getToken());

        if (!manager.getRequestResolverMap().containsKey(request.getToken()))
        {
            throw new IllegalArgumentException("The given token belongs to a not resolved request");
        }

        return getResolver(manager, manager.getRequestResolverMap().get(request.getToken()));
    }
}
