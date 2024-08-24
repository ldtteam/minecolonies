package com.minecolonies.core.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractWarehouseRequestResolver;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    protected boolean internalCanResolve(final Level level, final List<BuildingWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (requestToCheck.getRequest() instanceof IConcreteDeliverable)
        {
            return false;
        }

        int totalCount = 0;
        for (final BuildingWareHouse wareHouse : wareHouses)
        {
            for (@NotNull final BlockPos pos : wareHouse.getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof final TileEntityRack rack && !rack.isEmpty())
                    {
                        totalCount += rack.getItemCount(itemStack -> requestToCheck.getRequest().matches(itemStack));
                        if (totalCount >= requestToCheck.getRequest().getMinimumCount())
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
