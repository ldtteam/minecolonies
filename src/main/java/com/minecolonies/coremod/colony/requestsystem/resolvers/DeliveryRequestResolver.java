package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class DeliveryRequestResolver extends AbstractRequestResolver<Delivery>
{
    public DeliveryRequestResolver(
                                    @NotNull final ILocation location,
                                    @NotNull final IToken token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends Delivery> getRequestType()
    {
        return TypeToken.of(Delivery.class);
    }

    @Override
    public boolean canResolve(
                               @NotNull final IRequestManager manager, final IRequest<? extends Delivery> requestToCheck)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return false;
        }

        Colony colony = (Colony) manager.getColony();
        CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                        .stream()
                                        .filter(c -> c.getCitizenEntity() != null
                                                && requestToCheck.getRequest().getTarget().isReachableFromLocation(c.getCitizenEntity().getLocation()))
                                        .filter(c -> c.getJob() instanceof JobDeliveryman)
                                        .findFirst()
                                        .orElse(null);

        if (freeDeliveryMan == null)
        {
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    public List<IToken> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        Colony colony = (Colony) manager.getColony();
        CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                        .stream()
                                        .filter(c -> c.getCitizenEntity() != null && request.getRequest().getTarget().isReachableFromLocation(c.getCitizenEntity().getLocation()))
                                        .filter(c -> c.getJob() instanceof JobDeliveryman)
                                        .sorted(Comparator.comparing((CitizenData c) -> ((JobDeliveryman) c.getJob()).getTaskQueue().size())
                                                  .thenComparing(Comparator.comparing(c -> {
                                                      BlockPos targetPos = request.getRequest().getTarget().getInDimensionLocation();
                                                      BlockPos entityLocation = c.getCitizenEntity().getLocation().getInDimensionLocation();

                                                      return BlockPosUtil.getDistanceSquared(targetPos, entityLocation);
                                                  })))
                                        .findFirst()
                                        .orElse(null);

        if (freeDeliveryMan == null)
        {
            return null;
        }

        JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
        job.addRequest(request.getToken());

        return Lists.newArrayList();
    }

    @Nullable
    @Override
    public void resolve(
                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request) throws RuntimeException
    {
        //Noop. The delivery man will resolve it.
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public IRequest onRequestCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
            throws IllegalArgumentException
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            Colony colony = (Colony) manager.getColony();
            CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                            .stream()
                                            .filter(c -> c.getJob() instanceof JobDeliveryman && ((JobDeliveryman) c.getJob()).getTaskQueue().contains(request.getToken()))
                                            .findFirst()
                                            .orElse(null);

            if (freeDeliveryMan == null)
            {
                MineColonies.getLogger().error("Parent cancellation failed! Unknown request: " + request.getToken());
            }
            else
            {
                JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
                job.onTaskDeletion(request.getToken());
            }
        }

        return null;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        //We are not scheduling any child requests. So this should never be called.
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {
        //Noop
    }
}
