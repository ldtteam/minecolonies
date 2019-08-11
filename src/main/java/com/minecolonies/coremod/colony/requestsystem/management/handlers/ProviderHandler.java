package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class used to handle the inner workings of the request system with regards to providers.
 */
public class ProviderHandler implements IProviderHandler
{

    private final IStandardRequestManager manager;

    public ProviderHandler(final IStandardRequestManager manager) {this.manager = manager;}

    @Override
    public IRequestManager getManager()
    {
        return manager;
    }

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param provider The provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     *
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    @Override
    public Collection<IToken<?>> getRegisteredResolvers(final IRequestResolverProvider provider)
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
     * @param provider The provider that provides the resolvers.
     * @throws IllegalArgumentException is thrown when a provider is already registered.
     */
    @Override
    public void registerProvider(final IRequestResolverProvider provider)
    {
        final ImmutableList.Builder<IToken<?>> resolverListBuilder = new ImmutableList.Builder<>();
        resolverListBuilder.addAll(manager.getResolverHandler().registerResolvers(provider.getResolvers()));

        manager.getProviderResolverAssignmentDataStore().getAssignments().put(provider.getId(), resolverListBuilder.build());
        manager.getColony().markDirty();
    }

    @Override
    public void removeProvider(final IToken<?> token)
    {
        removeProviderInternal(token);
    }

    /**
     * Internal method that handles the reassignment
     *
     * @param token   The token of the provider that is being removed.
     * @throws IllegalArgumentException is thrown when the token is not registered to a provider, or when the data stored in the manager is in conflict.
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    void removeProviderInternal(final IToken<?> token)
    {
        manager.getLogger().info(String.format("Removing provider: %s", token));

        //Get the resolvers that are being removed.
        final Collection<IToken<?>> assignedResolvers = getRegisteredResolvers(token);

        processResolversForRemoval(assignedResolvers);

        //Removing the data from the maps.
        manager.getProviderResolverAssignmentDataStore().getAssignments().remove(token);
        manager.getColony().markDirty();
        manager.getLogger().debug(String.format("Removed provider: %s", token));
    }

    /**
     * Internal method that handles the removal off resolvers that are attached to a provider that is being removed.
     *
     * @param assignedResolvers The assigned resolvers that belong to the provider that is being removed.
     */
    @VisibleForTesting
    void processResolversForRemoval(final Collection<IToken<?>> assignedResolvers)
    {
        //Check if we have resolvers that need to be processed.
        if (assignedResolvers != null && !assignedResolvers.isEmpty())
        {
            //Skip if the resolver has no requests assigned.
            if (!manager.getRequestResolverRequestAssignmentDataStore().getAssignments().containsKey(resolverToken)
                    || manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken).isEmpty())
            {
                processResolverForRemoval(assignedResolvers, resolverToken);
            }
        }
    }

    /**
     * Internal method that handles the removal of a single resolvers that is attached to a provider that is being removed.
     *
     * @param assignedResolvers The list of resolvers which are being removed.
     * @param resolverToken The id of the resolver which is being removed, needs to be part of the assignedResolvers list.
     */
    @VisibleForTesting
    void processResolverForRemoval(final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
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
        manager.getLogger().debug("Removing resolver without assigned requests: " + resolverToken);
        manager.getRequestResolverRequestAssignmentDataStore().getAssignments().remove(resolverToken);

        manager.getResolverHandler().removeResolver(resolverToken);
    }

    /**
     * Internal method that is handling the removal of a resolver that has requests currently assigned to it.
     * Reassigns the assigned requests, using the provided list as blacklist.
     *
     * @param assignedResolvers The resolvers from the provider that is being removed.
     * @param resolverToken The particular resolver that is being removed. Needs to be part of the assignedResolvers list.
     */
    @VisibleForTesting
    void removeResolverWithAssignedRequests(@NotNull final Collection<IToken<?>> assignedResolvers, final IToken<?> resolverToken)
    {
        //Make sure that the resolver is actually supposed to be deleted
        Validate.isTrue(assignedResolvers.contains(resolverToken));

        //Clone the original list to modify it during iteration, if need be.
        final Collection<IToken<?>> assignedRequests = new ArrayList<>(manager.getRequestResolverRequestAssignmentDataStore().getAssignments().get(resolverToken));
        manager.getLogger().debug("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);

            LogHandler.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
        }

        removeResolverWithoutAssignedRequests(resolverToken);

        manager.getLogger().debug("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
    }

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param token   The token of the provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     *
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    @Override
    public Collection<IToken<?>> getRegisteredResolvers(@NotNull final IToken<?> token)
    {
        Collection<IToken<?>> result =  manager.getProviderResolverAssignmentDataStore().getAssignments().get(token);

        if (result == null)
	{
            return ImmutableList.of();
	}

        return result;
    }

    @Override
    public void removeProvider(@NotNull final IRequestResolverProvider provider)
    {
        removeProviderInternal(provider.getId());
    }
}
