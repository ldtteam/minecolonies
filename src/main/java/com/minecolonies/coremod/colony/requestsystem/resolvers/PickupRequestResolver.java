package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractRequestResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Resolver that handles pickup requests.
 * Pickups don't have much logic inherently, because the only important information
 * are the requester and the priority.
 * These resolvers are supposed to be provided by deliverymen.
 * <p>
 * Currently, this resolver will iterate through all available deliverymen and find
 * the one with the least amount of open requests, followed by the one that is nearest.
 * <p>
 * There is a tiny bit of (known) code-smell in here, since this resolver should either be global
 * or specific to a hut. Currently, it is a hut-specific resolver that acts as if it were global.
 * The performance impact is negligible though.
 */
public class PickupRequestResolver extends AbstractRequestResolver<Pickup>
{
    public PickupRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends Pickup> getRequestType()
    {
        return TypeConstants.PICKUP;
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends Pickup> requestToCheck)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return false;
        }

        final Colony colony = (Colony) manager.getColony();
        final ICitizenData freeDeliveryMan = colony.getCitizenManager().getCitizens()
                                               .stream()
                                               .filter(citizenData -> citizenData.getCitizenEntity()
                                                                        .map(entityCitizen -> requestToCheck.getRequester()
                                                                                                .getLocation()
                                                                                                .isReachableFromLocation(entityCitizen.getLocation()))
                                                                        .orElse(false))
                                               .filter(c -> c.getJob() instanceof JobDeliveryman)
                                               .findFirst()
                                               .orElse(null);
        return freeDeliveryMan != null;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Pickup> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        //We can do an instant get here, since we are already filtering on anything that has no entity.
        final ICitizenData freeDeliveryMan = colony.getCitizenManager()
                                               .getCitizens()
                                               .stream()
                                               .filter(citizenData -> citizenData.getCitizenEntity()
                                                                        .map(entityCitizen -> request.getRequester()
                                                                                                .getLocation()
                                                                                                .isReachableFromLocation(entityCitizen.getLocation()))
                                                                        .orElse(false))
                                               .filter(c -> c.getJob() instanceof JobDeliveryman)
                                               .filter(c -> ((JobDeliveryman) c.getJob()).isActive())
                                               .min(Comparator.comparing((ICitizenData c) -> ((JobDeliveryman) c.getJob()).getTaskQueue().size())
                                                      .thenComparing(Comparator.comparing(c -> {
                                                          BlockPos targetPos = request.getRequester().getLocation().getInDimensionLocation();
                                                          //We can do an instant get here, since we are already filtering on anything that has no entity.
                                                          BlockPos entityLocation = c.getCitizenEntity().get().getLocation().getInDimensionLocation();

                                                          return BlockPosUtil.getDistanceSquared(targetPos, entityLocation);
                                                      })))
                                               .orElse(null);

        if (freeDeliveryMan == null)
        {
            return null;
        }

        final JobDeliveryman job = (JobDeliveryman) freeDeliveryMan.getJob();
        job.addRequest(request.getId());

        return Lists.newArrayList();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Pickup> request) throws RuntimeException
    {
        //Noop. The delivery man will resolve it.
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Pickup> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Pickup> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Pickup> request)
    {
        if (!manager.getColony().getWorld().isRemote)
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

    @NotNull
    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public ITextComponent getRequesterDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN);
    }
}
