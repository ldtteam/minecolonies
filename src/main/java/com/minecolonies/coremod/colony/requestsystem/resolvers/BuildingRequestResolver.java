package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractBuildingDependentRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.RSConstants.CONST_BUILDING_RESOLVER_PRIORITY;

/**
 * Resolver that checks if a deliverable request is already in the building it is being requested from.
 */
public class BuildingRequestResolver extends AbstractBuildingDependentRequestResolver<IDeliverable>
{
    public BuildingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public int getPriority()
    {
        return CONST_BUILDING_RESOLVER_PRIORITY;
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeConstants.DELIVERABLE;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public boolean canResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        if (building instanceof BuildingWareHouse || !building.getCitizenForRequest(request.getId()).isPresent() || building.getCitizenForRequest(request.getId()).get().isRequestAsync(request.getId()))
        {
            return false;
        }

        final Predicate<ItemStack> pred = itemStack -> {
            if (ItemStackUtils.isEmpty(itemStack) || !request.getRequest().matches(itemStack))
            {
                return false;
            }

            if (!request.hasParent())
            {
                return true;
            }

            final IRequest<?> requestParent = manager.getRequestForToken(request.getParent());

            return !requestParent.getRequestOfType(IDeliverable.class).map(d -> d.matches(itemStack)).orElse(false);
        };

        return InventoryUtils.getCountFromBuilding(building, pred) > 0;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<? extends IDeliverable> request,
      @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = getCapabilityProviders(manager, building);

        final int totalRequested = request.getRequest().getCount();
        int totalAvailable = 0;
        for (final ICapabilityProvider tile : tileEntities)
        {
            final List<ItemStack> inv = InventoryUtils.filterProvider(tile, itemStack -> request.getRequest().matches(itemStack));
            for (final ItemStack stack : inv)
            {
                if (!stack.isEmpty())
                {
                    totalAvailable += stack.getCount();
                }
            }
        }

        for (final Map.Entry<ItemStorage, Integer> reserved : building.reservedStacks().entrySet())
        {
            if (request.getRequest().matches(reserved.getKey().getItemStack()))
            {
                totalAvailable = Math.max(0, totalAvailable - reserved.getValue());
                break;
            }
        }

        if (totalAvailable >= totalRequested)
        {
            return Lists.newArrayList();
        }

        if (totalAvailable >= request.getRequest().getMinimumCount())
        {
            return Lists.newArrayList();
        }

        final int totalRemainingRequired = totalRequested - totalAvailable;
        final IDeliverable remainingRequest = request.getRequest().copyWithCount(totalRemainingRequired);
        return Lists.newArrayList(manager.createRequest(this, remainingRequest));
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = getCapabilityProviders(manager, building);

        final int total = request.getRequest().getCount();
        int current = 0;
        final List<ItemStack> deliveries = new ArrayList<>();

        for (final ICapabilityProvider tile : tileEntities)
        {
            final List<ItemStack> inv = InventoryUtils.filterProvider(tile, itemStack -> request.getRequest().matches(itemStack));
            for (final ItemStack stack : inv)
            {
                if (!stack.isEmpty() && current < total)
                {
                    deliveries.add(stack);
                    current += stack.getCount();
                }
            }
        }

        request.addDelivery(deliveries);

        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        return null;
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }

    @NotNull
    private Set<ICapabilityProvider> getCapabilityProviders(
      @NotNull final IRequestManager manager,
      @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = Sets.newHashSet();
        tileEntities.add(building.getTileEntity());
        tileEntities.removeIf(Objects::isNull);
        return tileEntities;
    }
}
