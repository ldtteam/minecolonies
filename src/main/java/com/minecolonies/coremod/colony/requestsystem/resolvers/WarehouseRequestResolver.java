package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
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
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeToken.of(IDeliverable.class);
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            Colony colony = (Colony) manager.getColony();
            Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

            return wareHouses.stream().anyMatch(wareHouse -> wareHouse.hasMatchinItemStackInWarehouse(itemStack -> requestToCheck.getRequest().matches(itemStack)));
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
            ItemStack matchingStack = wareHouse.getFirstMatchingItemStackInWarehouse(itemStack -> request.getRequest().matches(itemStack));
            if (ItemStackUtils.isEmpty(matchingStack))
                continue;

            request.setDelivery(matchingStack);

            BlockPos itemStackPos = wareHouse.getPositionOfChestWithItemStack(itemStack -> ItemStack.areItemsEqual(itemStack, matchingStack));
            ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, itemStackPos, wareHouse.getWorld().provider.getDimension());

            Delivery delivery = new Delivery(itemStackLocation, request.getRequester().getRequesterLocation(), matchingStack);

            IToken requestToken = manager.createRequest(request.getRequester(), delivery);

            return ImmutableList.of(requestToken);
        }

        return Lists.newArrayList();
    }

    @Nullable
    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
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
}
