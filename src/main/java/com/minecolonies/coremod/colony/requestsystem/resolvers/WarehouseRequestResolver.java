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
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import com.minecolonies.coremod.tileentities.ITileEntityWareHouse;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
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
        super(token, location,  TypeConstants.DELIVERABLE);
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeConstants.DELIVERABLE;
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            final Set<ITileEntityWareHouse> wareHouses = getWareHousesInColony(colony);
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
    public List<IToken<?>> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final Set<ITileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        List<IToken<?>> deliveries = Lists.newArrayList();
        int remainingCount = request.getRequest().getCount();

        tileentities:
        for (final ITileEntityWareHouse wareHouse : wareHouses)
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
                final ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, itemStackPos, wareHouse.getWorld().provider.getDimension());

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
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        manager.updateRequestState(request.getId(), RequestState.COMPLETED);
    }

    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    private static Set<ITileEntityWareHouse> getWareHousesInColony(final Colony colony)
    {
        return colony.getBuildingManager().getBuildings().values().stream()
                 .filter(building -> building instanceof BuildingWareHouse)
                 .map(building -> (ITileEntityWareHouse) building.getTileEntity())
                 .collect(Collectors.toSet());
    }



    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest token)
    {
        return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_BUILDING_WAREHOUSE_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_WAREHOUSE_RESOLVER_PRIORITY;
    }

    /**
     * Method called by the request system to notify this requester that a request is complete.
     * Is also called by the request system, when a request has been overruled, and as such
     * completed by the player, instead of the initially assigned resolver.
     *
     * @param manager The request manager that has completed the given request.
     * @param request The request that has been completed.
     */
    @NotNull
    @Override
    public void onRequestedRequestCompleted(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    /**
     * Method called by the request system to notify this requester that a request has been cancelled.
     *
     * @param manager The request manager that has cancelled the given request.
     * @param request The request that has been cancelled.
     */
    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //TODO: What todo when a delivery request gets cancelled?
    }
}
