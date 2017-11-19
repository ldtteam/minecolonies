package com.minecolonies.coremod.colony.requestsystem.management;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Describes the {@link StandardRequestManager} data access. Is only used for internal handling.
 */
public interface IStandardRequestManager extends IRequestManager
{
    @NotNull
    BiMap<IToken, IRequestResolverProvider> getProviderBiMap();

    @NotNull
    BiMap<IToken, IRequestResolver> getResolverBiMap();

    @NotNull
    BiMap<IToken, IRequest> getRequestBiMap();

    @NotNull
    Map<IToken, ImmutableCollection<IToken>> getProviderResolverMap();

    @NotNull
    Map<IToken, Set<IToken>> getResolverRequestMap();

    @NotNull
    Map<IToken, IToken> getRequestResolverMap();

    @NotNull
    Map<TypeToken, Collection<IRequestResolver>> getRequestClassResolverMap();

    @NotNull
    boolean isDataSimulation();

    @NotNull
    boolean isResolvingSimulation();
}
