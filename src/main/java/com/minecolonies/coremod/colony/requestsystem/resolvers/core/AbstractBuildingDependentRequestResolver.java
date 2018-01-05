package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Abstract class used as a base class for request resolver that handle request coming from the building they are originating from.
 * @param <R> The type of request that they handle
 */
public abstract class AbstractBuildingDependentRequestResolver<R extends IRequestable> extends AbstractRequestResolver<R>
{

    public AbstractBuildingDependentRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolve(
      @NotNull final IRequestManager manager, final IRequest<? extends R> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            if (requestToCheck.getRequester() instanceof BuildingBasedRequester)
            {
                final BuildingBasedRequester requester = (BuildingBasedRequester) requestToCheck.getRequester();
                final ILocation requesterLocation = requester.getRequesterLocation();
                if (requesterLocation.equals(getRequesterLocation()))
                {
                    return requester.getBuilding()
                             .map(r -> (AbstractBuilding) r)
                             .map(b -> canResolveForBuilding(manager, requestToCheck, b))
                             .orElse(false);
                }
            }
        }

        return false;
    }

    public abstract boolean canResolveForBuilding(@NotNull final IRequestManager manager, final @NotNull IRequest<? extends R> request, final @NotNull AbstractBuilding building);

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final BuildingBasedRequester requester = (BuildingBasedRequester) request.getRequester();
        return requester.getBuilding()
          .map(r -> (AbstractBuilding) r)
          .map(b -> attemptResolveForBuilding(manager, request, b))
          .orElseGet(null);
    }

    @Nullable
    public abstract List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final AbstractBuilding building);

    @Override
    public void resolve(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final BuildingBasedRequester requester = (BuildingBasedRequester) request.getRequester();
        requester.getBuilding()
          .map(r -> (AbstractBuilding) r)
          .ifPresent(b -> resolveForBuilding(manager, request, b));
    }

    public abstract void resolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final AbstractBuilding building);
}
