package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface IProviderHandler
{
    IRequestManager getManager();

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param provider The provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    Collection<IToken<?>> getRegisteredResolvers(IRequestResolverProvider provider);

    /**
     * Method used to register a provider to a given manager.
     *
     * @param provider The provider that provides the resolvers.
     * @throws IllegalArgumentException is thrown when a provider is already registered.
     */
    void registerProvider(IRequestResolverProvider provider);

    void removeProvider(IToken<?> token);

    /**
     * Method used to get the registered resolvers for a given provider.
     *
     * @param token The token of the provider you are requesting the resolvers for.
     * @return The registered resolvers that belong to the given provider.
     * @throws IllegalArgumentException when the token is not belonging to a registered provider.
     */
    Collection<IToken<?>> getRegisteredResolvers(@NotNull IToken<?> token);

    void removeProvider(@NotNull IRequestResolverProvider provider);
}
