package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IBuildingBasedRequester extends IRequester
{
    /**
     * Gets the building for a given request.
     *
     * @param manager the manager.
     * @param request the token.
     * @return the IRequester or empty.
     */
    default Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }
}
