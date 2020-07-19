package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseRequestResolver extends AbstractWarehouseRequestResolver
{
    public WarehouseRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    protected boolean internalCanResolve(final Set<TileEntityWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if(requestToCheck.getRequest() instanceof IConcreteDeliverable)
        {
            return false; 
        }
        return wareHouses.stream()
                .anyMatch(wareHouse -> wareHouse.hasMatchingItemStackInWarehouse(itemStack -> requestToCheck.getRequest().matches(itemStack),
                requestToCheck.getRequest().getMinimumCount()));
    }

    @Override
    protected AbstractWarehouseRequestResolver newInstanceOfSelf(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        return new WarehouseRequestResolver(location, token);
    }
}
