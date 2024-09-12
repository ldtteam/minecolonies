package com.minecolonies.core.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import org.jetbrains.annotations.NotNull;

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
    protected int getWarehouseInternalCount(final BuildingWareHouse wareHouse, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (requestToCheck.getRequest() instanceof IConcreteDeliverable)
        {
            return 0;
        }

        int totalCount = 0;
        final AbstractTileEntityWareHouse tileEntityWareHouse = wareHouse.getTileEntity();
        if (tileEntityWareHouse != null)
        {
            totalCount += tileEntityWareHouse.getCountInWarehouse(itemStack -> requestToCheck.getRequest().matches(itemStack), requestToCheck.getRequest().getCount());
            if (totalCount >= requestToCheck.getRequest().getCount())
            {
                return totalCount;
            }
        }

        return totalCount;
    }
}
