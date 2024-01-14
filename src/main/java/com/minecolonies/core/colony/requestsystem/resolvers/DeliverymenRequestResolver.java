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
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.core.colony.jobs.JobDeliveryman;
import com.minecolonies.core.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

        return !getResolveAbleDeliverymen(manager).isEmpty();
    }

    /**
     * Get the deliverymen we can resolve requests for
     *
     * @param manager request manager
     * @return list of citizens
     */
    public List<ICitizenData> getResolveAbleDeliverymen(@NotNull final IRequestManager manager)
    {
        final List<ICitizenData> citizenList = new ArrayList<>();
        final Colony colony = (Colony) manager.getColony();
        final IWareHouse wareHouse = colony.getBuildingManager().getBuilding(getLocation().getInDimensionLocation(), IWareHouse.class);
        if (wareHouse == null)
        {
            return citizenList;
        }

        for (final ICitizenData data : wareHouse.getFirstModuleOccurance(CourierAssignmentModule.class).getAssignedCitizen())
        {
            if (data.isWorking())
            {
                citizenList.add(data);
            }
        }

        return citizenList;
    }

    @Override
    public int getSuitabilityMetric(@NotNull final IRequest<? extends R> request)
    {
        return (int) BlockPosUtil.getDistance(request.getRequester().getLocation().getInDimensionLocation(), getLocation().getInDimensionLocation());
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return null;
        }

        ICitizenData chosenCourier = null;

        Tuple<Double, Integer> bestScore = null;
        for (final ICitizenData citizen : getResolveAbleDeliverymen(manager))
        {
            if (citizen.isWorking())
            {
                Tuple<Double, Integer> localScore = ((JobDeliveryman) citizen.getJob()).getScoreForDelivery(request);
                if (bestScore == null || localScore.getA() < bestScore.getA())
                {
                    bestScore = localScore;
                    chosenCourier = citizen;
                }
            }
        }

        if (chosenCourier == null)
        {
            return null;
        }

        return Lists.newArrayList();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request) throws RuntimeException
    {
        ICitizenData chosenCourier = null;

        Tuple<Double, Integer> bestScore = null;
        for (final ICitizenData citizen : getResolveAbleDeliverymen(manager))
        {
            if (citizen.isWorking())
            {
                Tuple<Double, Integer> localScore = ((JobDeliveryman) citizen.getJob()).getScoreForDelivery(request);
                if (bestScore == null || localScore.getA() < bestScore.getA())
                {
                    bestScore = localScore;
                    chosenCourier = citizen;
                }
            }
        }

        if (chosenCourier == null)
        {
            return;
        }

        final JobDeliveryman job = (JobDeliveryman) chosenCourier.getJob();
        job.addRequest(request.getId(), bestScore.getB());
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

            if (freeDeliveryMan == null)
            {
                Log.getLogger().error("Parent cancellation of delivery request failed! Unknown request: " + request.getId(), new Exception());
            }
            else
            {
                final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
                job.onTaskDeletion(request.getId());
            }
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
        return Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN);
    }
}
