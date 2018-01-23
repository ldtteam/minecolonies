package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IBuildingBasedRequester extends IRequester
{
    Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token);
}
