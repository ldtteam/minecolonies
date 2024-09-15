package com.minecolonies.core.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.INonExhaustiveDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    protected int getWarehouseInternalCount(final BuildingWareHouse wareHouse, final IRequest<? extends IDeliverable> requestToCheck)
    {
        final IDeliverable deliverable = requestToCheck.getRequest();
        if (!(deliverable instanceof IConcreteDeliverable))
        {
            return 0;
        }

        boolean ignoreNBT = false;
        boolean ignoreDamage = false;
        if (deliverable instanceof Stack stack)
        {
            ignoreNBT = !stack.matchNBT();
            ignoreDamage = !stack.matchDamage();
        }
        int totalCount = 0;
        for (final ItemStack possible : ((IConcreteDeliverable) deliverable).getRequestedItems())
        {
            if (requestToCheck.getRequest() instanceof INonExhaustiveDeliverable neDeliverable)
            {
                totalCount += Math.max(0, InventoryUtils.hasBuildingEnoughElseCount(wareHouse,
                  new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getCount() + neDeliverable.getLeftOver()) - neDeliverable.getLeftOver());
            }
            else
            {
                totalCount += InventoryUtils.hasBuildingEnoughElseCount(wareHouse,
                  new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getCount());
            }

            if (totalCount >= requestToCheck.getRequest().getCount())
            {
                return totalCount;
            }
        }
        return totalCount;
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
