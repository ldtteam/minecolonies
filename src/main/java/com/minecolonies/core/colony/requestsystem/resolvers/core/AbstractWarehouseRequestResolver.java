package com.minecolonies.core.colony.requestsystem.resolvers.core;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.INonExhaustiveDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.core.tileentities.TileEntityWareHouse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getDefaultDeliveryPriority;
import static com.minecolonies.api.util.constant.RSConstants.CONST_WAREHOUSE_RESOLVER_PRIORITY;

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
    protected abstract boolean internalCanResolve(final List<TileEntityWareHouse> wareHouses, final IRequest<? extends IDeliverable> requestToCheck);

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

        if (!manager.getColony().getWorld().isClientSide)
        {
            if (!isRequestChainValid(manager, requestToCheck))
            {
                return false;
            }

            final Colony colony = (Colony) manager.getColony();

            try
            {
                return internalCanResolve(getWareHousesInColony(colony, requestToCheck.getRequester().getLocation().getInDimensionLocation()), requestToCheck);
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
        if (manager.getColony().getWorld().isClientSide)
        {
            return Lists.newArrayList();
        }

        if (!(manager.getColony() instanceof Colony))
        {
            return Lists.newArrayList();
        }

        final Colony colony = (Colony) manager.getColony();

        final List<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony, request.getRequester().getLocation().getInDimensionLocation());

        final int totalRequested = request.getRequest().getCount();
        int totalAvailable = 0;
        if (request.getRequest() instanceof INonExhaustiveDeliverable)
        {
            totalAvailable -= ((INonExhaustiveDeliverable) request.getRequest()).getLeftOver();
        }
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

        if (totalAvailable < 0)
        {
            totalAvailable = 0;
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
        if (manager.getColony().getWorld().isClientSide)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final List<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony, completedRequest.getRequester().getLocation().getInDimensionLocation());

        List<IRequest<?>> deliveries = Lists.newArrayList();
        int remainingCount = completedRequest.getRequest().getCount();

        final Map<ItemStorage, Integer> storages = new HashMap<>();

        final int keep = completedRequest.getRequest() instanceof INonExhaustiveDeliverable ? ((INonExhaustiveDeliverable) completedRequest.getRequest()).getLeftOver() : 0;

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

                int leftOver = tuple.getA().getCount();
                if (keep > 0)
                {
                    int kept = storages.getOrDefault(new ItemStorage(tuple.getA()), 0);
                    if (kept < keep)
                    {
                        if (leftOver + kept <= keep)
                        {
                            storages.put(new ItemStorage(tuple.getA()), storages.getOrDefault(new ItemStorage(tuple.getA()), 0) + tuple.getA().getCount());
                            continue;
                        }
                        int toKeep = (leftOver + kept) - keep;
                        leftOver-=toKeep;
                        storages.put(new ItemStorage(tuple.getA()), storages.getOrDefault(new ItemStorage(tuple.getA()), 0) + toKeep);
                    }
                }

                int count = Math.min(remainingCount, leftOver);
                final ItemStack matchingStack = tuple.getA().copy();
                matchingStack.setCount(count);

                completedRequest.addDelivery(matchingStack);

                final ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, tuple.getB(), wareHouse.getLevel().dimension());

                final Delivery delivery =
                  new Delivery(itemStackLocation, completedRequest.getRequester().getLocation(), matchingStack, getDefaultDeliveryPriority(true));


                final IToken<?> requestToken = manager.createRequest(this, delivery);
                deliveries.add(manager.getRequestForToken(requestToken));
                remainingCount -= count;

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
     * Use to get the ordered list of all warehouses.
     * @param colony the colony in question.
     * @return the ordered list.
     */
    protected static List<TileEntityWareHouse> getWareHousesInColony(final Colony colony, final BlockPos requesterPosition)
    {
        final List<TileEntityWareHouse> wareHouses = new ArrayList<>();
        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (building instanceof BuildingWareHouse && building.getTileEntity() != null)
            {
                wareHouses.add((TileEntityWareHouse) building.getTileEntity());
            }
        }

        wareHouses.sort((w1, w2) ->
        {
            final double dist1 = w1.getPosition().distSqr(requesterPosition);
            final double dist2 = w2.getPosition().distSqr(requesterPosition);
            return Double.compare(dist1, dist2);
        });

        return wareHouses;
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
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_BUILDING_WAREHOUSE_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_WAREHOUSE_RESOLVER_PRIORITY;
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
