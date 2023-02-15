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
import net.minecraft.world.item.ItemStack;
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
        if(requestToCheck.getRequest() instanceof final IConcreteDeliverable deliverable)
        {
            boolean ignoreNBT = false;
            boolean ignoreDamage = false;
            if (deliverable instanceof final Stack stack)
            {
                if (!stack.matchNBT())
                {
                    ignoreNBT = true;
                }
                if (!stack.matchDamage())
                {
                    ignoreDamage = true;
                }
            }
            for(final ItemStack possible : deliverable.getRequestedItems())
            {
                for (final TileEntityWareHouse wareHouse : wareHouses)
                {
                    if (requestToCheck.getRequest() instanceof final INonExhaustiveDeliverable nonExhaustDeli)
                    {
                        if (wareHouse.hasMatchingItemStackInWarehouse(possible, requestToCheck.getRequest().getMinimumCount(), ignoreNBT, ignoreDamage, nonExhaustDeli.getLeftOver()))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if (wareHouse.hasMatchingItemStackInWarehouse(possible, requestToCheck.getRequest().getMinimumCount(), ignoreNBT, ignoreDamage, 0))
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
