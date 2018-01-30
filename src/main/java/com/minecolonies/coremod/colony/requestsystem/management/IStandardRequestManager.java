package com.minecolonies.coremod.colony.requestsystem.management;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.data.*;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
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
    IRequestIdentitiesDataStore getRequestIdentitiesDataStore();

    @NotNull
    IRequestResolverIdentitiesDataStore getRequestResolverIdentitiesDataStore();

    @NotNull
    IProviderResolverAssignmentDataStore getProviderResolverAssignmentDataStore();

    @NotNull
    IRequestResolverRequestAssignmentDataStore getRequestResolverRequestAssignmentDataStore();

    @NotNull
    IRequestableTypeRequestResolverAssignmentDataStore getRequestableTypeRequestResolverAssignmentDataStore();

    int getCurrentVersion();

    void setCurrentVersion(int currentVersion);
}
