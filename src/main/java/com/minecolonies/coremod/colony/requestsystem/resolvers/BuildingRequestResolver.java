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
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Resolver that checks if a deliverable request is already in the building it is being requested from.
 */
public class BuildingRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    public BuildingRequestResolver(
                                    @NotNull final ILocation location,
                                    @NotNull final IToken token)
    {
        super(location, token);
    }

    @Override
    public int getPriority()
    {
        return CONST_DEFAULT_RESOLVER_PRIORITY + 100;
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeToken.of(IDeliverable.class);
    }

    @Override
    public boolean canResolve(
                               @NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return false;
        }

        if (!(requestToCheck.getRequester() instanceof BuildingBasedRequester))
        {
            return false;
        }

        if (!requestToCheck.getRequester().getRequesterLocation().equals(getRequesterLocation()))
        {
            return false;
        }

        AbstractBuilding building = getBuildingFromRequest(manager, requestToCheck);

        List<TileEntity> tileEntities = new ArrayList<>();
        tileEntities.add(building.getTileEntity());
        tileEntities.addAll(building.getAdditionalCountainers().stream().map(manager.getColony().getWorld()::getTileEntity).collect(Collectors.toSet()));
        tileEntities.removeIf(Objects::isNull);

        return tileEntities.stream()
                 .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> requestToCheck.getRequest().matches(itemStack)))
                 .anyMatch(itemStacks -> !itemStacks.isEmpty());
    }

    @Nullable
    @Override
    public List<IToken> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (canResolve(manager, request))
        {
            return Lists.newArrayList();
        }

        return null;
    }

    @Nullable
    @Override
    public void resolve(
                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request) throws RuntimeException
    {
        AbstractBuilding building = getBuildingFromRequest(manager, request);

        List<TileEntity> tileEntities = new ArrayList<>();
        tileEntities.add(building.getTileEntity());
        tileEntities.addAll(building.getAdditionalCountainers().stream().map(manager.getColony().getWorld()::getTileEntity).collect(Collectors.toSet()));

        request.setDelivery(tileEntities.stream()
                              .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStack -> request.getRequest().matches(itemStack)))
                              .filter(itemStacks -> !itemStacks.isEmpty())
                              .flatMap(List::stream)
                              .findFirst()
                              .orElse(ItemStackUtils.EMPTY));

        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
    }

    @NotNull
    private AbstractBuilding getBuildingFromRequest(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request) throws RuntimeException
    {
        BuildingBasedRequester requester = (BuildingBasedRequester) request.getRequester();
        return requester.getBuilding().map(r -> {
            if (r instanceof AbstractBuildingView && manager.getColony() instanceof Colony)
            {
                final Colony colony = (Colony) manager.getColony();
                return colony.getBuildingManager().getBuilding(((AbstractBuildingView) r).getID());
            }
            else
            {
                return (AbstractBuilding) r;
            }
        }).orElseThrow(() -> new IllegalStateException("Unknown building."));
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public IRequest onRequestCancelledOrOverruled(
                                                   @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request) throws IllegalArgumentException
    {
        return null;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {

    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {

    }
}
