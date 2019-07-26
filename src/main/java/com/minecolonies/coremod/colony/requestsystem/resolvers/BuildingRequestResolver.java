package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractBuildingDependentRequestResolver;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        super(token, location, TypeConstants.DELIVERABLE);
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
    public boolean canResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final IBuilding building)
    {
        final List<TileEntity> tileEntities = new ArrayList<>();
        tileEntities.add((TileEntity) building.getTileEntity());
        tileEntities.addAll(building.getAdditionalCountainers().stream().map(manager.getColony().getWorld()::getTileEntity).collect(Collectors.toSet()));
        tileEntities.removeIf(Objects::isNull);

        if (building.getCitizenForRequest(request.getId()).isPresent() && building.getCitizenForRequest(request.getId()).get().isRequestAsync(request.getId()))
        {
            return false;
        }

        return tileEntities.stream()
                 .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> request.getRequest().matches(itemStack)))
                 .anyMatch(itemStacks -> !itemStacks.isEmpty());
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final IBuilding building)
    {
        return Lists.newArrayList();
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final IBuilding building)
    {
        final List<TileEntity> tileEntities = new ArrayList<>();
        tileEntities.add(building.getTileEntity());

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

        manager.updateRequestState(request.getId(), RequestState.COMPLETED);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return null;
    }

    @Nullable
    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return;
    }


    @Override
    public void onRequestedRequestCompleted(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {

    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager,@NotNull final IToken<?> token)
    {
        final IRequest<?> cancelledRequest = manager.getRequestForToken(token);
        if (cancelledRequest != null && cancelledRequest.hasParent())
        {
            final IRequest<?> parentRequest = manager.getRequestForToken(cancelledRequest.getParent());
            if (parentRequest.getState() != RequestState.CANCELLED && parentRequest.getState() != RequestState.OVERRULED)
            {
                manager.updateRequestState(cancelledRequest.getParent(), RequestState.CANCELLED);
            }
        }
    }
}
