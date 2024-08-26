package com.minecolonies.core.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.INonExhaustiveDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    protected boolean internalCanResolve(final Level level, final List<BuildingWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck)
    {
        final IDeliverable deliverable = requestToCheck.getRequest();

        if (deliverable instanceof IConcreteDeliverable)
        {
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
                for (final BuildingWareHouse wareHouse : wareHouses)
                {
                    if (wareHouse.getTileEntity() == null)
                    {
                        continue;
                    }

                    if (requestToCheck.getRequest() instanceof INonExhaustiveDeliverable neDeliverable)
                    {
                        totalCount += Math.max(0, wareHouse.getTileEntity().getCountInWarehouse(new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getMinimumCount()) - neDeliverable.getLeftOver());
                    }
                    else
                    {
                        totalCount += wareHouse.getTileEntity().getCountInWarehouse(new ItemStorage(possible, requestToCheck.getRequest().getMinimumCount(), ignoreDamage, ignoreNBT), requestToCheck.getRequest().getMinimumCount());
                    }

                    if (totalCount >= requestToCheck.getRequest().getMinimumCount())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
