package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.BuildingWareHouse;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseRequestResolver extends AbstractRequestResolver<IDeliverable>
{

    public WarehouseRequestResolver(
                                     @NotNull final ILocation location,
                                     @NotNull final IToken token)
    {
        super(location, token);
    }

    @Override
    public Class<? extends IDeliverable> getRequestType()
    {
        return IDeliverable.class;
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            Colony colony = (Colony) manager.getColony();
            Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

            return wareHouses.stream().anyMatch(wareHouse -> wareHouse.hasMatchinItemStackInWarehouse(requestToCheck.getRequest()::matches));
        }

        return false;
    }

    @Nullable
    @Override
    @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
    /**
     * Moving the curly braces really makes the code hard to read.
     */
    public List<IToken> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
            return null;

        Colony colony = (Colony) manager.getColony();
        Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        for(TileEntityWareHouse wareHouse : wareHouses) {
            ItemStack matchingStack = wareHouse.getFirstMatchingItemStackInWarehouse(request.getRequest()::matches);
            if (ItemStackUtils.isEmpty(matchingStack))
                continue;

            request.setDelivery(matchingStack);
        }



        final TileEntity tileEntity = manager.getColony().getWorld().getTileEntity(getRequesterLocation().getInDimensionLocation());

        if (tileEntity instanceof TileEntityWareHouse)
        {
            final TileEntityWareHouse wareHouse = (TileEntityWareHouse) tileEntity;
            final BlockPos pos = wareHouse.getPositionOfChestWithItemStack(request.getRequest());

            request.setResult(request.getRequest().copy());
            return Lists.newArrayList(manager.createRequest(new WarehouseChestDeliveryRequester(this, manager.getFactoryController().getNewInstance(UUID.randomUUID(),
              new TypeToken<IToken>() {}), manager.getFactoryController().getNewInstance(pos, new TypeToken<ILocation>() {}), request.getToken()),
              new Delivery(manager.getFactoryController().getNewInstance(
                pos,
                new TypeToken<ILocation>() {}), request.getRequester().getDeliveryLocation(), request.getRequest().)));
        }

        return Lists.newArrayList();
    }

    @Nullable
    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        //Noop delivery has been completed
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest onParentCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return null;
    }

    @Nullable
    @Override
    public void onResolvingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
    }

    private Set<TileEntityWareHouse> getWareHousesInColony(Colony colony)
    {
        return colony.getBuildings().values().stream()
                 .filter(building -> building instanceof BuildingWareHouse)
                 .map(building -> (TileEntityWareHouse) building.getTileEntity())
                 .collect(Collectors.toSet());
    }

    @SuppressWarnings("squid:S2972")
    /**
     * We have this class the way it is for a reason.
     */
    private final class WarehouseChestDeliveryRequester implements IRequester
    {
        private final WarehouseRequestResolver warehouseRequestResolver;
        private final IToken                   id;
        private final ILocation                location;
        private final IToken                   itemStackRequestToken;

        private WarehouseChestDeliveryRequester(
                                                 final WarehouseRequestResolver warehouseRequestResolver,
                                                 final IToken id,
                                                 final ILocation location,
                                                 final IToken itemStackRequestToken)
        {
            this.warehouseRequestResolver = warehouseRequestResolver;
            this.id = id;
            this.location = location;
            this.itemStackRequestToken = itemStackRequestToken;
        }

        @Override
        public IToken getRequesterId()
        {
            return id;
        }

        @NotNull
        @Override
        public ILocation getRequesterLocation()
        {
            return location;
        }

        @NotNull
        @Override
        public void onRequestComplete(@NotNull final IToken token)
        {
            warehouseRequestResolver.onRequestComplete(itemStackRequestToken);
        }
    }
}
