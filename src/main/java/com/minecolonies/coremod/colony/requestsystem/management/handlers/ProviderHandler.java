package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.apache.commons.lang3.Validate;
import sun.net.www.content.audio.basic;

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
        final Collection<IToken<?>> result = manager.getProviderResolverAssignmentDataStore().getAssignments().get(provider.getId());
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

        manager.getProviderResolverAssignmentDataStore().getAssignments().put(provider.getId(), resolverListBuilder.build());
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
    @VisibleForTesting
    @SuppressWarnings(Suppression.UNCHECKED)
    static void removeProviderInternal(final IStandardRequestManager manager, final IToken<?> token)
    {
        LogHandler.log("Removing provider: " + token);

        //Get the resolvers that are being removed.
        final Collection<IToken<?>> assignedResolvers = getRegisteredResolvers(manager, token);

        processResolversForRemoval(manager, assignedResolvers);

        //Removing the data from the maps.
        manager.getProviderResolverAssignmentDataStore().getAssignments().remove(token);
        manager.getColony().markDirty();
        LogHandler.log("Removed provider: " + token);
    }

    /**
     * Internal method that handles the removal off resolvers that are attached to a provider that is being removed.
     *
     * @param manager The manager from which a provider is being removed.
     * @param assignedResolvers The assigned resolvers that belong to the provider that is being removed.
     */
    @VisibleForTesting
    static void processResolversForRemoval(final IStandardRequestManager manager, final Collection<IToken<?>> assignedResolvers)
    {
        //Check if we have resolvers that need to be processed.
        if (assignedResolvers != null && !assignedResolvers.isEmpty())
        {
            //For each resolver process them.
            for (final IToken<?> resolverToken : assignedResolvers)
            {
                processResolverForRemoval(manager, assignedResolvers, resolverToken);
            }
        }
    }

    /**
     * Internal method that handles the removal of a single resolvers that is attached to a provider that is being removed.
     *
     * @param manager The manager from which a provider is being removed.
     * @param assignedResolvers The list of resolvers which are being removed.
     * @param resolverToken The id of the resolver which is being removed, needs to be part of the assignedResolvers list.
     */
    @VisibleForTesting
    static void processResolverForRemoval(final IStandardRequestManager manager, final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
    {
        //Make sure that the resolver is actually supposed to be deleted.
        Validate.isTrue(assignedResolvers.contains(resolverToken));

        //Skip if the resolver has no requests assigned.
        if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolverToken)
              || manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken).isEmpty())
        {
            //No requests assigned so lets process this resolver as such.
            removeResolverWithoutAssignedRequests(manager, resolverToken);
            return;
        }

        //This resolver has currently requests assigned, which needs to be handled separately
        removeResolverWithAssignedRequests(manager, assignedResolvers, resolverToken);
    }

    /**
     * Internal method that is handling the removal of a resolver that has requests currently assigned to it.
     * Reassigns the assigned requests, using the provided list as blacklist.
     *
     * @param manager The manager from which a provider is being removed.
     * @param assignedResolvers The resolvers from the provider that is being removed.
     * @param resolverToken The particular resolver that is being removed. Needs to be part of the assignedResolvers list.
     */
    @VisibleForTesting
    static void removeResolverWithAssignedRequests(final IStandardRequestManager manager, final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
    {
        //Make sure that the resolver is actually supposed to be deleted
        Validate.isTrue(assignedResolvers.contains(resolverToken));

        //Clone the original list to modify it during iteration, if need be.
        final Collection<IToken<?>> assignedRequests = new ArrayList<>(manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken));
        LogHandler.log("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);

        //Get all assigned requests and reassign them.
        for (final IToken<?> requestToken : assignedRequests)
        {
            manager.reassignRequest(requestToken, assignedResolvers);
        }

        removeResolverWithoutAssignedRequests(manager, resolverToken);

        LogHandler.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
    }

    /**
     * Internal method that is handling the removal of a resolver that has no requests assigned to it.
     *
     * @param manager The manager from which a provider is being removed.
     * @param resolverToken The resolver from the provider which is being removed, but has no requests assigned anymore.
     */
    @VisibleForTesting
    static void removeResolverWithoutAssignedRequests(final IStandardRequestManager manager, final IToken<?> resolverToken)
    {
        LogHandler.log("Removing resolver without assigned requests: " + resolverToken);
        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolverToken);

        ResolverHandler.removeResolver(manager, resolverToken);
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
        removeProviderInternal(manager, provider.getId());
    }
}
