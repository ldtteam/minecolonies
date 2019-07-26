package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.requestsystem.exceptions.NoBuildingWithAssignedRequestFoundException;
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
      @NotNull final IToken<?> token,
      @NotNull final ILocation location,
      @NotNull final TypeToken<? extends R> requestType)
    {
        super(token, location, requestType);
    }

    @Override
    @NotNull
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (request.getRequester() instanceof IBuildingBasedRequester)
        {
            final IBuildingBasedRequester requester = (IBuildingBasedRequester) request.getRequester();
            final ILocation requesterLocation = requester.getLocation();
            if (requesterLocation.equals(getLocation()))
            {
                return requester.getBuilding(manager, request);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean canResolve(
      @NotNull final IRequestManager manager, final IRequest<? extends R> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final ILocation requesterLocation = requestToCheck.getRequester().getLocation();
            if (requesterLocation.equals(getLocation()))
            {
                final Optional<IBuilding> building = getBuilding(manager, requestToCheck.getId()).map(r -> (IBuilding) r);
                if (!building.isPresent())
                    return false;

                return building.map(b -> canResolveForBuilding(manager, requestToCheck, b)).orElse(false);
            }
        }

        return false;
    }

    public abstract boolean canResolveForBuilding(@NotNull final IRequestManager manager, final @NotNull IRequest<? extends R> request, final @NotNull IBuilding building);

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final Optional<IBuilding> buildingOptional = getBuilding(manager, request.getId()).map(r -> (IBuilding) r);
        if (!buildingOptional.isPresent())
            return Lists.newArrayList();

        final IBuilding building = buildingOptional.get();
        return attemptResolveForBuilding(manager, request, building);
    }

    @Nullable
    public abstract List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final IBuilding building);

    @Override
    public void resolve(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final Optional<IBuilding> buildingOptional = getBuilding(manager, request.getId()).map(r -> (IBuilding) r);
        if (!buildingOptional.isPresent())
            throw new NoBuildingWithAssignedRequestFoundException("Can not find building from request: " + request.toString());

        final IBuilding building = buildingOptional.get();
        resolveForBuilding(manager, request, building);
    }

    public abstract void resolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, @NotNull final IBuilding building);
}
