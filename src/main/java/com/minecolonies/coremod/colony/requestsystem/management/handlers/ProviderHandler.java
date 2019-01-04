package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableList;
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
    public static Collection<IToken<?>> getRegisteredResolvers(final IStandardRequestManager manager, final IRequestResolverProvider provider)
    {
        final Collection<IToken<?>> result = manager.getProviderResolverAssignmentDataStore().getAssignments().get(provider.getToken());
        if (result == null)
	{
            return ImmutableList.of();
	}

        return result;
    }

    /**
     * Method used to register a provider to a given manager.
     *
     * @param manager  The manager to register them to.
     * @param provider The provider that provides the resolvers.
     * @throws IllegalArgumentException is thrown when a provider is already registered.
     */
    public static void registerProvider(final IStandardRequestManager manager, final IRequestResolverProvider provider)
    {
        final ImmutableList.Builder<IToken<?>> resolverListBuilder = new ImmutableList.Builder<>();
        resolverListBuilder.addAll(ResolverHandler.registerResolvers(manager, provider.getResolvers()));

        manager.getProviderResolverAssignmentDataStore().getAssignments().put(provider.getToken(), resolverListBuilder.build());
        manager.getColony().markDirty();
    }

    public static void removeProvider(final IStandardRequestManager manager, final IToken<?> token)
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
    public static void removeProviderInternal(final IStandardRequestManager manager, final IToken<?> token)
    {
        LogHandler.log("Removing provider: " + token);

        //Get the resolvers that are being removed.
        final Collection<IToken<?>> assignedResolvers = getRegisteredResolvers(manager, token);

        if(assignedResolvers == null)
        {
            return;
        }

        for (final IToken<?> resolverToken : assignedResolvers)
        {
            //Skip if the resolver has no requests assigned.
            if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolverToken)
                    || manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken).isEmpty())
            {
                LogHandler.log("Removing resolver without assigned requests: " + resolverToken);
                manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolverToken);

                ResolverHandler.removeResolver(manager, resolverToken);

                continue;
            }

            //Clone the original list to modify it during iteration, if need be.
            final Collection<IToken<?>> assignedRequests = new ArrayList<>(manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken));
            LogHandler.log("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);

            //Get all assigned requests and reassign them.
            for (final IToken<?> requestToken : assignedRequests)
            {
                manager.reassignRequest(requestToken, assignedResolvers);
            }

            ResolverHandler.removeResolver(manager, resolverToken);

            LogHandler.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
        }

        //Removing the data from the maps.
        manager.getProviderResolverAssignmentDataStore().getAssignments().remove(token);
        manager.getColony().markDirty();
        LogHandler.log("Removed provider: " + token);
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
    public static Collection<IToken<?>> getRegisteredResolvers(final IStandardRequestManager manager, final IToken<?> token)
    {
        Collection<IToken<?>> result =  manager.getProviderResolverAssignmentDataStore().getAssignments().get(token);

        if (result == null)
	{
            return ImmutableList.of();
	}

        return result;
    }

    public static void removeProvider(final IStandardRequestManager manager, final IRequestResolverProvider provider)
    {
        removeProviderInternal(manager, provider.getToken());
    }
}
