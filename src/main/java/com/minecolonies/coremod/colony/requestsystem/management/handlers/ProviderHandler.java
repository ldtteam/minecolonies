package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class used to handle the inner workings of the request system with regards to providers.
 */
public final class ProviderHandler
{

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param manager  The manager to pull the data from.
     * @param provider The provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     *
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    public static ImmutableCollection<IToken<?>> getRegisteredResolvers(final IStandardRequestManager manager, final IRequestResolverProvider provider)
      throws IllegalArgumentException
    {
        //Check if the token is registered.
        getProvider(manager, provider.getToken());

        return manager.getProviderResolverMap().get(provider.getToken());
    }

    /**
     * Method used to get a provider from a token.
     *
     * @param token The token to get the provider form.
     * @return The provider that corresponds to the given token
     *
     * @throws IllegalArgumentException when no provider is not registered with the given token.
     */
    public static IRequestResolverProvider getProvider(final IStandardRequestManager manager, final IToken<?> token) throws IllegalArgumentException
    {
        if (!manager.getProviderBiMap().containsKey(token))
        {
            throw new IllegalArgumentException("The given token for a provider is not registered");
        }

        return manager.getProviderBiMap().get(token);
    }

    /**
     * Method used to register a provider to a given manager.
     *
     * @param manager  The manager to register them to.
     * @param provider The provider that provides the resolvers.
     * @throws IllegalArgumentException is thrown when a provider is already registered.
     */
    public static void registerProvider(final IStandardRequestManager manager, final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        if (manager.getProviderBiMap().containsKey(provider.getToken()) ||
              manager.getProviderBiMap().containsValue(provider))
        {
            throw new IllegalArgumentException("The given provider is already registered");
        }

        manager.getProviderBiMap().put(provider.getToken(), provider);

        final ImmutableList.Builder<IToken<?>> resolverListBuilder = new ImmutableList.Builder<>();
        resolverListBuilder.addAll(ResolverHandler.registerResolvers(manager, provider.getResolvers()));

        manager.getProviderResolverMap().put(provider.getToken(), resolverListBuilder.build());
    }

    public static void removeProvider(final IStandardRequestManager manager, final IToken<?> token) throws IllegalArgumentException
    {
        removeProviderInternal(manager, token);
    }

    /**
     * Internal method that handles the reassignment
     *
     * @param manager The manager that is being modified.
     * @param token   The token of the provider that is being removed.
     * @throws IllegalArgumentException is thrown when the token is not registered to a provider, or when the data stored in the manager is in conflict.
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    public static void removeProviderInternal(final IStandardRequestManager manager, final IToken<?> token) throws IllegalArgumentException
    {
        final IRequestResolverProvider provider = getProvider(manager, token);

        LogHandler.log("Removing provider: " + provider);

        //Get the resolvers that are being removed.
        final ImmutableCollection<IToken<?>> assignedResolvers = getRegisteredResolvers(manager, token);
        for (final IToken<?> resolverToken : assignedResolvers)
        {
            //If no requests are assigned to this resolver skip.
            if (!manager.getResolverRequestMap().containsKey(resolverToken))
            {
                continue;
            }

            //Skip if the resolver has no requests assigned.
            if (manager.getResolverRequestMap().get(resolverToken).size() == 0)
            {
                LogHandler.log("Removing resolver without assigned requests: " + resolverToken);
                manager.getResolverRequestMap().remove(resolverToken);

                ResolverHandler.removeResolver(manager, resolverToken);

                continue;
            }

            //Clone the original list to modify it during iteration, if need be.
            final Collection<IToken<?>> assignedRequests = new ArrayList<>(manager.getResolverRequestMap().get(resolverToken));
            LogHandler.log("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);

            //Get all assigned requests and reassign them.
            for (final IToken<?> requestToken : assignedRequests)
            {
                LogHandler.log("Removing assigned request: " + requestToken + " from resolver: " + resolverToken);

                //No need to notify the resolver of the cancellation, It is getting removed anyway.
                //In that case: All resources lost, restart on different resolver.
                //Also cancel all registered child task:
                manager.getResolverRequestMap().get(resolverToken).remove(requestToken);
                manager.getRequestResolverMap().remove(requestToken);

                LogHandler.log("Cancelling all child requests of:" + requestToken);

                //Check if the request has children.
                final IRequest assignedRequest = RequestHandler.getRequest(manager, requestToken);
                if (assignedRequest.hasChildren())
                {
                    //Iterate over all children and call there onRequestCancelledOrOverruled method to get a new cleanup parent.
                    for (final Object objectToken :
                      assignedRequest.getChildren())
                    {
                        if (objectToken instanceof IToken)
                        {
                            final IToken<?> childToken = (IToken<?>) objectToken;
                            final IRequest childRequest = RequestHandler.getRequest(manager, childToken);

                            //Check if the child has been assigned. If not, no work done, no cleanup needed.
                            if (RequestHandler.isAssigned(manager, childToken))
                            {
                                //Get the child request
                                final IRequestResolver childResolver = ResolverHandler.getResolverForRequest(manager, childToken);
                                final IRequest cleanUpRequest = childResolver.onRequestCancelledOrOverruled(manager, childRequest);

                                //Switch out the parent, and add the old child to the followup request as new child
                                if (cleanUpRequest != null)
                                {
                                    cleanUpRequest.addChild(childToken);
                                    childRequest.setParent(cleanUpRequest.getToken());

                                    //Assign the new followup request if it is not assigned yet.
                                    if (!RequestHandler.isAssigned(manager, cleanUpRequest.getToken()))
                                    {
                                        RequestHandler.assignRequest(manager, cleanUpRequest);
                                    }
                                }
                            }
                        }
                    }
                }

                LogHandler.log("Starting reassignment of: " + requestToken + " - Assigned to: " + resolverToken);

                RequestHandler.assignRequest(manager, assignedRequest, assignedResolvers);

                if (assignedRequest.getState().ordinal() < RequestState.RECEIVED.ordinal())
                {
                    LogHandler.log("Finished reassignment of: " + requestToken + " - Assigned to: " + manager.getRequestResolverMap().get(requestToken));
                }
            }

            ResolverHandler.removeResolver(manager, resolverToken);

            LogHandler.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
        }

        //Removing the data from the maps.
        manager.getProviderBiMap().remove(provider.getToken());
        manager.getProviderResolverMap().remove(provider.getToken());

        LogHandler.log("Removed provider: " + provider);
    }

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param manager The manager to pull the data from.
     * @param token   The token of the provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     *
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    public static ImmutableCollection<IToken<?>> getRegisteredResolvers(final IStandardRequestManager manager, final IToken<?> token) throws IllegalArgumentException
    {
        //Check if the token is registered.
        getProvider(manager, token);

        return manager.getProviderResolverMap().get(token);
    }

    public static void removeProvider(final IStandardRequestManager manager, final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        final IRequestResolverProvider registeredProvider = getProvider(manager, provider.getToken());

        if (!registeredProvider.equals(provider))
        {
            throw new IllegalArgumentException("The given providers token is registered to a different provider!");
        }

        removeProviderInternal(manager, provider.getToken());
    }
}
