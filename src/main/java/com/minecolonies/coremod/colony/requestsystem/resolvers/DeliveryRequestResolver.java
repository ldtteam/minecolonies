package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class DeliveryRequestResolver extends AbstractRequestResolver<Delivery>
{
    public DeliveryRequestResolver(
                                    @NotNull final ILocation location,
                                    @NotNull final IToken<?> token)
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

        final Colony colony = (Colony) manager.getColony();
        final CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
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
    public List<IToken<?>> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
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

        final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
        job.addRequest(request.getToken());

        return Lists.newArrayList();
    }

    @Override
    public void resolve(
                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request) throws RuntimeException
    {
        //Noop. The delivery man will resolve it.
    }

    @Nullable
    @Override
    public IRequest<?> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            final CitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
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
                final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
                job.onTaskDeletion(request.getToken());
            }
        }

        return null;
    }

    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Delivery> request)
    {
        onRequestCancelled(manager, request);
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //We are not scheduling any child requests. So this should never be called.
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        Log.getLogger().error("cancelled");
    }
}
