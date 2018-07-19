package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
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
        super(location, token);
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
            final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);
            wareHouses.removeIf(Objects::isNull);

            try
            {
                return wareHouses.stream().anyMatch(wareHouse -> wareHouse.hasMatchinItemStackInWarehouse(itemStack -> requestToCheck.getRequest().matches(itemStack)));
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
        final Set<TileEntityWareHouse> wareHouses = getWareHousesInColony(colony);

        for (final TileEntityWareHouse wareHouse : wareHouses)
        {
            ItemStack matchingStack = wareHouse.getFirstMatchingItemStackInWarehouse(itemStack -> request.getRequest().matches(itemStack));
            if (ItemStackUtils.isEmpty(matchingStack))
            {
                continue;
            }

            matchingStack = matchingStack.copy();
            matchingStack.setCount(Math.min(request.getRequest().getCount(), matchingStack.getCount()));

            final ItemStack deliveryStack = matchingStack.copy();
            request.setDelivery(deliveryStack.copy());

            final BlockPos itemStackPos = wareHouse.getPositionOfChestWithItemStack(itemStack -> ItemStack.areItemsEqual(itemStack, deliveryStack));
            final ILocation itemStackLocation = manager.getFactoryController().getNewInstance(TypeConstants.ILOCATION, itemStackPos, wareHouse.getWorld().provider.getDimension());

            final Delivery delivery = new Delivery(itemStackLocation, request.getRequester().getRequesterLocation(), deliveryStack.copy());

            final IToken<?> requestToken = manager.createRequest(new WarehouseRequestResolver(request.getRequester().getRequesterLocation(), request.getToken()), delivery);

            return ImmutableList.of(requestToken);
        }

        return Lists.newArrayList();
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
    }

    @Nullable
    @Override
    public IRequest<?> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return null;
    }

    @Override
    public void onRequestBeingOverruled(
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
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //Somebody cancelled the delivery.
        //reassign the parent request.
        final IRequest request = manager.getRequestForToken(token);
        if (request.hasParent())
        {
            final IRequest parent = manager.getRequestForToken(token);

            if (parent.getState() != RequestState.CANCELLED && parent.getState() != RequestState.OVERRULED)
                manager.reassignRequest(parent.getToken(), ImmutableList.of());
        }
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_BUILDING_WAREHOUSE_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_WAREHOUSE_RESOLVER_PRIORITY;
    }
}
