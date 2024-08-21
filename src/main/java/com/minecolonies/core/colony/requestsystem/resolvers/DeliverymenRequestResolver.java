package com.minecolonies.core.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.core.colony.buildings.modules.WarehouseRequestQueueModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.colony.jobs.JobDeliveryman;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Resolver which resolves requests to it given deliverymen. Resolving is based on how well a request fits a dman, evaluated through request scores.
 */
public abstract class DeliverymenRequestResolver<R extends IRequestable> extends AbstractRequestResolver<R>
{
    public DeliverymenRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends R> requestToCheck)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return false;
        }
        final Colony colony = (Colony) manager.getColony();

        if (colony.getBuildingManager().getBuilding(requestToCheck.getRequester().getLocation().getInDimensionLocation()) instanceof BuildingWareHouse buildingWareHouse
              && !buildingWareHouse.getID().equals(getLocation().getInDimensionLocation()))
        {
            return false;
        }

        return hasCouriers(manager);
    }

    /**
     * Get the deliverymen we can resolve requests for
     *
     * @param manager request manager
     * @return list of citizens
     */
    public boolean hasCouriers(@NotNull final IRequestManager manager)
    {
        final Colony colony = (Colony) manager.getColony();
        final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return false;
        }

       return !wareHouse.getModule(BuildingModules.WAREHOUSE_COURIERS).getAssignedCitizen().isEmpty();
    }

    @Override
    public int getSuitabilityMetric(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        final IWareHouse wareHouse = manager.getColony().getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        final int distance = (int) BlockPosUtil.getDistance(request.getRequester().getLocation().getInDimensionLocation(), getLocation().getInDimensionLocation());
        if (wareHouse == null)
        {
            return distance;
        }
        return Math.max(distance/10, 1) + wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE).getMutableRequestList().size();
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        if (manager.getColony().getWorld().isClientSide || !hasCouriers(manager))
        {
            return null;
        }

        return Lists.newArrayList();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request) throws RuntimeException
    {
        final Colony colony = (Colony) manager.getColony();
        final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return;
        }

        if (wareHouse.getModule(BuildingModules.WAREHOUSE_COURIERS).getAssignedCitizen().isEmpty())
        {
            return;
        }

        final WarehouseRequestQueueModule module = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
        module.addRequest(request.getId());
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> completedRequest)
    {
        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            final Colony colony = (Colony) manager.getColony();
            final ICitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                                   .stream()
                                                   .filter(c -> c.getJob() instanceof JobDeliveryman && ((JobDeliveryman) c.getJob()).getTaskQueue().contains(request.getId()))
                                                   .findFirst()
                                                   .orElse(null);

            if (freeDeliveryMan != null)
            {
                final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
                job.onTaskDeletion(request.getId());
            }

            final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
            if (wareHouse == null)
            {
                return;
            }

            final WarehouseRequestQueueModule module = wareHouse.getModule(BuildingModules.WAREHOUSE_REQUEST_QUEUE);
            module.getMutableRequestList().remove(request.getId());
        }
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
    public MutableComponent getRequesterDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN);
    }

    @Override
    public boolean isValid()
    {
        // Always valid
        return true;
    }
}
