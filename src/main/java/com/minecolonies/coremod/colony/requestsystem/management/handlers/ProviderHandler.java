package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.management.IProviderHandler;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.jetbrains.annotations.NotNull;

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
     * @param token The token of the provider that is being removed.
     * @throws IllegalArgumentException is thrown when the token is not registered to a provider, or when the data stored in the manager is in conflict.
     */
    @VisibleForTesting
    void removeProviderInternal(final IToken<?> token)
    {
        manager.log(String.format("Removing provider: %s", token));

        //Get the resolvers that are being removed.
        final Collection<IToken<?>> assignedResolvers = getRegisteredResolvers(token);

        processResolversForRemoval(assignedResolvers);

        //Removing the data from the maps.
        manager.getProviderResolverAssignmentDataStore().getAssignments().remove(token);
        manager.getColony().markDirty();
        manager.log(String.format("Removed provider: %s", token));
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
            //For each resolver process them.
            for (final IToken<?> resolverToken : assignedResolvers)
            {
                manager.getResolverHandler().processResolverForRemoval(assignedResolvers, resolverToken);
            }
        }
    }

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param token The token of the provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    @Override
    public Collection<IToken<?>> getRegisteredResolvers(@NotNull final IToken<?> token)
    {
        Collection<IToken<?>> result = manager.getProviderResolverAssignmentDataStore().getAssignments().get(token);

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
