package com.minecolonies.coremod.colony.requestsystem.management;

import com.minecolonies.api.colony.requestsystem.data.*;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.*;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import org.jetbrains.annotations.NotNull;

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

    IProviderHandler getProviderHandler();

    IRequestHandler getRequestHandler();

    IResolverHandler getResolverHandler();

    ITokenHandler getTokenHandler();

    IUpdateHandler getUpdateHandler();

    int getCurrentVersion();

    void setCurrentVersion(int currentVersion);
}
