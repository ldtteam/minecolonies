package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
    protected boolean internalCanResolve(final Set<TileEntityWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck)
    {
        final IDeliverable deliverable = requestToCheck.getRequest();
        if(deliverable instanceof IConcreteDeliverable)
        {
            for(final ItemStack possible : ((IConcreteDeliverable) deliverable).getRequestedItems())
            {
                for (final TileEntityWareHouse wareHouse : wareHouses)
                {
                    if (wareHouse.hasMatchingItemStackInWarehouse(possible, requestToCheck.getRequest().getMinimumCount()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
