package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.INonExhaustiveDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseConcreteRequestResolver extends AbstractWarehouseRequestResolver
{
    public WarehouseConcreteRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    protected boolean internalCanResolve(final List<TileEntityWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck)
    {
        final IDeliverable deliverable = requestToCheck.getRequest();

        if(deliverable instanceof IConcreteDeliverable)
        {
            boolean ignoreNBT = false;
            if (deliverable instanceof Stack && !((Stack) requestToCheck.getRequest()).matchNBT())
            {
                ignoreNBT = true;
            }
            for(final ItemStack possible : ((IConcreteDeliverable) deliverable).getRequestedItems())
            {
                for (final TileEntityWareHouse wareHouse : wareHouses)
                {
                    if (requestToCheck.getRequest() instanceof INonExhaustiveDeliverable)
                    {
                        if (wareHouse.hasMatchingItemStackInWarehouse(possible, requestToCheck.getRequest().getMinimumCount(), ignoreNBT, ((INonExhaustiveDeliverable) requestToCheck.getRequest()).getLeftOver()))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if (wareHouse.hasMatchingItemStackInWarehouse(possible, requestToCheck.getRequest().getMinimumCount(), ignoreNBT))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
