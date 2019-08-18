package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.RSConstants.CONST_WAREHOUSE_RESOLVER_PRIORITY;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    public WarehouseRequestResolver(
                                     @NotNull final ILocation location,
                                     @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeConstants.DELIVERABLE;
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);
            wareHouses.removeIf(Objects::isNull);

            try
            {
                return wareHouses.stream().anyMatch(wareHouse -> wareHouse.hasMatchingItemStackInWarehouse(itemStack -> requestToCheck.getRequest().matches(itemStack), requestToCheck.getRequest().getCount()));
            }
            catch (Exception e)
            {
                Log.getLogger().error(e);
            }
        }

        return false;
    }

    @Nullable
    @Override
    @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
    /**
     * Moving the curly braces really makes the code hard to read.
     */
    public List<IToken<?>> attemptResolveRequest(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        List<IToken<?>> deliveries = Lists.newArrayList();
        int remainingCount = request.getRequest().getCount();

        tileentities:
        for (final TileEntityWareHouse wareHouse : wareHouses)
        {
            final List<ItemStack> targetStacks = wareHouse.getMatchingItemStacksInWarehouse(itemStack -> request.getRequest().matches(itemStack));
            for (final ItemStack stack :
              targetStacks)
            {
                if (ItemStackUtils.isEmpty(stack))	
		{
                    continue;
		}

                final ItemStack matchingStack = stack.copy();
                matchingStack.setCount(Math.min(remainingCount, matchingStack.getCount()));

                final ItemStack deliveryStack = matchingStack.copy();
                request.addDelivery(deliveryStack.copy());

                final BlockPos itemStackPos = wareHouse.getPositionOfChestWithItemStack(itemStack -> ItemStack.areItemsEqual(itemStack, deliveryStack));
                final ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, itemStackPos, wareHouse.getWorld().getDimension().getType().getId());

                final Delivery delivery = new Delivery(itemStackLocation, request.getRequester().getLocation(), deliveryStack.copy());

                final IToken<?> requestToken = manager.createRequest(new WarehouseRequestResolver(request.getRequester().getLocation(), request.getId()), delivery);

                deliveries.add(requestToken);
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
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        manager.updateRequestState(request.getId(), RequestState.COMPLETED);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public void onAssignedRequestBeingCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    private static Set<TileEntityWareHouse> getWareHousesInColony(final Colony colony)
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
