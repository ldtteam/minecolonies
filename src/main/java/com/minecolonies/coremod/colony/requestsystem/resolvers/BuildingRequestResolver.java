package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractBuildingDependentRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.minecolonies.api.util.RSConstants.CONST_BUILDING_RESOLVER_PRIORITY;

/**
 * Resolver that checks if a deliverable request is already in the building it is being requested from.
 */
public class BuildingRequestResolver extends AbstractBuildingDependentRequestResolver<IDeliverable>
{
    public BuildingRequestResolver(
                                    @NotNull final ILocation location,
                                    @NotNull final IToken<?> token)
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
    public void onAssignedRequestBeingCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public boolean canResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = getCapabilityProviders(manager, building);

        if (building.getCitizenForRequest(request.getId()).isPresent() && building.getCitizenForRequest(request.getId()).get().isRequestAsync(request.getId()))
        {
            return false;
        }


        return tileEntities.stream()
          .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> request.getRequest().matches(itemStack)))
          .filter(itemStack -> !itemStack.isEmpty())
          .flatMap(List::stream)
          .filter(itemStack -> {
              if (!request.hasParent())
                  return true;

              final IRequest<?> requestParent = manager.getRequestForToken(request.getParent());

              return !requestParent.getRequestOfType(IDeliverable.class).map(d -> d.matches(itemStack)).orElse(false);
          })
          .mapToInt(ItemStack::getCount)
          .sum() > 0;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = getCapabilityProviders(manager, building);

        final int totalRequested = request.getRequest().getCount();
        final int totalAvailable = tileEntities.stream()
                                     .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> request.getRequest().matches(itemStack)))
                                     .filter(itemStacks -> !itemStacks.isEmpty())
                                     .flatMap(List::stream)
                                     .mapToInt(ItemStack::getCount)
                                     .sum();

        if (totalAvailable >= totalRequested)
            return Lists.newArrayList();

        final int totalRemainingRequired = totalRequested - totalAvailable;
        final IDeliverable remainingRequest = request.getRequest().copyWithCount(totalRemainingRequired);

        if (!building.requiresCompleteRequestFulfillment())
        {
            if (building.getCitizenForRequest(request.getId()).isPresent())
            {
                return Lists.newArrayList(building.getCitizenForRequest(request.getId()).get().createRequestAsync(remainingRequest));
            }
            return Lists.newArrayList();
        }
        else
        {
            return Lists.newArrayList(manager.createRequest(this, remainingRequest));
        }
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        final Set<ICapabilityProvider> tileEntities = getCapabilityProviders(manager, building);

        final int total = request.getRequest().getCount();
        final AtomicInteger current = new AtomicInteger(0);

        tileEntities.stream()
          .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> request.getRequest().matches(itemStack)))
          .filter(itemStacks -> !itemStacks.isEmpty())
          .flatMap(List::stream)
          .forEach(stack ->
          {
              if (current.get() < total)
              {
                  request.addDelivery(stack);
                  current.getAndAdd(stack.getCount());
              }
          });

        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
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
