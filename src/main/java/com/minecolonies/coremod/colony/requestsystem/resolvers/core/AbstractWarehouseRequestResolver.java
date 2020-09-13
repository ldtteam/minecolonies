package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getDefaultDeliveryPriority;
import static com.minecolonies.api.util.RSConstants.CONST_WAREHOUSE_RESOLVER_PRIORITY;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public abstract class AbstractWarehouseRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    public AbstractWarehouseRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    /**
     * Check to see if this object type is the same as the request
     */
    protected boolean isRequestFromSelf(final IRequest<?> requestToCheck)
    {
        return requestToCheck.getRequester().getClass().equals(this.getClass());
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeConstants.DELIVERABLE;
    }

    /**
     * Override to implement decendent specific checks during canResolveRequest
     * @param wareHouses
     * @param requestToCheck
     * @return
     */
    protected abstract boolean internalCanResolve(final Set<TileEntityWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck);

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (requestToCheck.getRequester() instanceof BuildingBasedRequester)
        {
            final BuildingBasedRequester requester = ((BuildingBasedRequester) requestToCheck.getRequester());
            final Optional<IRequester> building = requester.getBuilding(manager, requestToCheck.getRequester().getId());
            if (building.isPresent() && building.get() instanceof BuildingWareHouse)
            {
                return false;
            }
        }

        if (!manager.getColony().getWorld().isRemote)
        {
            if (!isRequestChainValid(manager, requestToCheck))
            {
                return false;
            }

            final Colony colony = (Colony) manager.getColony();
            final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);
            wareHouses.removeIf(Objects::isNull);

            try
            {
                return internalCanResolve(wareHouses, requestToCheck);
            }
            catch (Exception e)
            {
                Log.getLogger().error(e);
            }
        }
        return false;
    }

    /**
     * Use to verify that a request chain is valid, and doesn't contain recursive cycles.
     * @param manager
     * @param requestToCheck
     * @return
     */
    public boolean isRequestChainValid(@NotNull final IRequestManager manager, final IRequest<?> requestToCheck)
    {
        if (isRequestFromSelf(requestToCheck))
        {
            return false;
        }

        if (!requestToCheck.hasParent())
        {
            return true;
        }

        final IRequest<?> parentRequest = manager.getRequestForToken(requestToCheck.getParent());

        //Should not happen but just to be sure.
        if (parentRequest == null)
        {
            return true;
        }

        return isRequestChainValid(manager, parentRequest);
    }

    /*
     * Moving the curly braces really makes the code hard to read.
     */
    @Nullable
    @Override
    @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return Lists.newArrayList();
        }

        if (!(manager.getColony() instanceof Colony))
        {
            return Lists.newArrayList();
        }

        final Colony colony = (Colony) manager.getColony();

        final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        final int totalRequested = request.getRequest().getCount();
        int totalAvailable = 0;
        for (final TileEntityWareHouse tile : wareHouses)
        {
            final List<Tuple<ItemStack, BlockPos>> inv = tile.getMatchingItemStacksInWarehouse(itemStack -> request.getRequest().matches(itemStack));
            for (final Tuple<ItemStack, BlockPos> stack : inv)
            {
                if (!stack.getA().isEmpty())
                {
                    totalAvailable += stack.getA().getCount();
                }
            }
        }

        if (totalAvailable >= totalRequested || totalAvailable >= request.getRequest().getMinimumCount())
        {
            return Lists.newArrayList();
        }

        final int totalRemainingRequired = totalRequested - totalAvailable;
        final IDeliverable remainingRequest = request.getRequest().copyWithCount(totalRemainingRequired);
        return Lists.newArrayList(manager.createRequest(this, remainingRequest));
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        List<IRequest<?>> deliveries = Lists.newArrayList();
        int remainingCount = completedRequest.getRequest().getCount();

        tileentities:
        for (final TileEntityWareHouse wareHouse : wareHouses)
        {
            final List<Tuple<ItemStack, BlockPos>> targetStacks = wareHouse.getMatchingItemStacksInWarehouse(itemStack -> completedRequest.getRequest().matches(itemStack));
            for (final Tuple<ItemStack, BlockPos> tuple : targetStacks)
            {
                if (ItemStackUtils.isEmpty(tuple.getA()))
                {
                    continue;
                }

                final ItemStack matchingStack = tuple.getA().copy();
                matchingStack.setCount(Math.min(remainingCount, matchingStack.getCount()));

                final ItemStack deliveryStack = matchingStack.copy();
                completedRequest.addDelivery(deliveryStack);

                final ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, tuple.getB(), wareHouse.getWorld().getDimension().getType().getId());

                final Delivery delivery =
                  new Delivery(itemStackLocation, completedRequest.getRequester().getLocation(), deliveryStack, getDefaultDeliveryPriority(true));

                final IToken<?> requestToken =
                  manager.createRequest(manager.getFactoryController()
                                          .getNewInstance(TypeToken.of(this.getClass()), completedRequest.getRequester().getLocation(), completedRequest.getId()), delivery);
                deliveries.add(manager.getRequestForToken(requestToken));
                remainingCount -= ItemStackUtils.getSize(matchingStack);

                if (remainingCount <= 0)
                {
                    break tileentities;
                }
            }
        }

        return deliveries.isEmpty() ? null : deliveries;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    /**
     * Use to get the set of all warehouses
     * @param colony
     * @return
     */
    protected static Set<TileEntityWareHouse> getWareHousesInColony(final Colony colony)
    {
        return colony.getBuildingManager().getBuildings().values().stream()
                 .filter(building -> building instanceof BuildingWareHouse)
                 .map(building -> (TileEntityWareHouse) building.getTileEntity())
                 .collect(Collectors.toSet());
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
    }

    @NotNull
    @Override
    public ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_BUILDING_WAREHOUSE_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_WAREHOUSE_RESOLVER_PRIORITY;
    }
}
