package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Abstract class used as a base class for request resolver that handle request coming from the building they are originating from.
 * @param <R> The type of request that they handle
 */
public abstract class AbstractBuildingDependentRequestResolver<R extends IRequestable> extends AbstractRequestResolver<R> implements IBuildingBasedRequester
{

    public AbstractBuildingDependentRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolveRequest(
      @NotNull final IRequestManager manager, final IRequest<? extends R> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final ILocation requesterLocation = requestToCheck.getRequester().getLocation();
            if (requesterLocation.equals(getLocation()))
            {
                final Optional<AbstractBuilding> building = getBuilding(manager, requestToCheck.getId()).map(r -> (AbstractBuilding) r);
                return building.map(b -> canResolveForBuilding(manager, requestToCheck, b)).orElse(false);
            }
        }

        return false;
    }

    public abstract boolean canResolveForBuilding(@NotNull final IRequestManager manager, final @NotNull IRequest<? extends R> request, final @NotNull AbstractBuilding building);

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        return attemptResolveForBuilding(manager, request, building);
    }

    @Nullable
    public abstract List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final AbstractBuilding building);

    @Override
    public void resolveRequest(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        resolveForBuilding(manager, request, building);
    }

    public abstract void resolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final AbstractBuilding building);
}
