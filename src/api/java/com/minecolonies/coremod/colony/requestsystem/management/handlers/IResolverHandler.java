package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.Collection;
import java.util.function.Predicate;

public interface IResolverHandler
{
    IRequestManager getManager();

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    Collection<IToken<?>> registerResolvers(IRequestResolver<?>... resolvers);

    /**
     * Method used to register a resolver.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track which resolver are registered.
     * </p>
     *
     * @param resolver The resolver to register
     * @return The token of the newly registered resolver
     * @throws IllegalArgumentException is thrown when either the token attached to the resolver is already registered or the resolver is already registered with a different token
     */
    IToken<?> registerResolver(IRequestResolver<? extends IRequestable> resolver);

    /**
     * Method used to register multiple resolvers simultaneously
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to register.
     * @return The tokens of the resolvers that when registered.
     * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
     */
    Collection<IToken<?>> registerResolvers(Collection<IRequestResolver<?>> resolvers);

    /**
     * Method used to remove a registered resolver.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param token The token of the resolver to remove.
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    void removeResolver(IToken<?> token);

    /**
     * Method used to remove a registered resolver.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolver The resolver to remove
     * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
     */
    void removeResolver(IRequestResolver<?> resolver);

    /**
     * Method to get all requests currently assigned to a resolver.
     *
     * @param resolver The resolver to get the requests from.
     * @return A collection with requests tokens.
     */
    Collection<IToken<?>> getRequestsAssignedToResolver(IRequestResolver<?> resolver);

    /**
     * Method to get a resolver from a given token.
     * <p>
     * <p>
     * Is only used internally. Querries the resolverBiMap to get the resolver for a given Token.
     * </p>
     *
     * @param token The token of the resolver to look up.
     * @return The resolver registered with the given token.
     * @throws IllegalArgumentException is thrown when the given token is not registered to any IRequestResolver
     */
    IRequestResolver<? extends IRequestable> getResolver(IToken<?> token);

    void removeResolverInternal(IRequestResolver<?> resolver);

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    void removeResolvers(IRequestResolver<?>... resolvers);

    /**
     * Method used to remove a multiple registered resolvers.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverBiMap that is used to track resolvers.
     * </p>
     *
     * @param resolvers The resolvers to remove.
     * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
     */
    void removeResolvers(Iterable<IRequestResolver<?>> resolvers);

    /**
     * Method used to add a request to a resolver.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param resolver The resolver to add the request to.
     * @param request  The request to add to the resolver.
     */
    void addRequestToResolver(IRequestResolver<?> resolver, IRequest<?> request);

    /**
     * Method used to remove a request from a resolver.
     * <p>
     * <p>
     * Is only used internally. The method modifies the resolverRequestMap that is used to track which resolver handles which request.
     * </p>
     *
     * @param resolver The resolver to remove the given request from.
     * @param request  The request to remove.
     * @throws IllegalArgumentException is thrown when the resolver is unknown, or when the given request is not registered to the given resolver.
     */
    void removeRequestFromResolver(IRequestResolver<?> resolver, IRequest<?> request);

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param requestToken The token of a request a the assigned resolver is requested for.
     * @return The resolver for the request with the given token.
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    IRequestResolver<? extends IRequestable> getResolverForRequest(IToken<?> requestToken);

    /**
     * Method used to get a resolver from a given request token.
     *
     * @param request The request a the assigned resolver is requested for.
     * @return The resolver for the request.
     * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
     */
    IRequestResolver<? extends IRequestable> getResolverForRequest(IRequest<?> request);

    /**
     * Method used to reassign requests based on a predicate
     *
     * @param shouldTriggerReassign the predicate to determine whether a request should be reassigned
     */
    void onColonyUpdate(Predicate<IRequest> shouldTriggerReassign);
}
