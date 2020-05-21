package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.MAX_DELIVERYMAN_PRIORITY;

public class PublicWorkerCraftingProductionResolver extends AbstractCraftingProductionResolver<PublicCrafting>
{
    /**
     * Constructor to initialize.
     *
     * @param location the location.
     * @param token    the id.
     */
    public PublicWorkerCraftingProductionResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token, PublicCrafting.class);
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            removeRequestFromTaskList(request, colony);
        }
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> completedRequest)
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony || !completedRequest.hasParent())
        {
            //Remove it from the task list.
            removeRequestFromTaskList(completedRequest, colony);

            //This is the crafting that got completed.
            //We go up the tree one level to get the actual request.
            //Get the requester for that request and ask where he wants his stuff delivered.
            final IRequest<?> parentRequest = manager.getRequestForToken(completedRequest.getParent());
            final IRequester parentRequestRequester = parentRequest.getRequester();

            if (parentRequestRequester.getLocation().equals(getLocation()))
            {
                return null;
            }

            final List<IRequest<?>> deliveries = Lists.newArrayList();

            completedRequest.getDeliveries().forEach(parentRequest::addDelivery);
            completedRequest.getDeliveries().forEach(itemStack -> {
                // Followup-Deliveries for crafting requests have the highest possible priority. They are important after all!
                final Delivery delivery = new Delivery(getLocation(), parentRequestRequester.getLocation(), itemStack, MAX_DELIVERYMAN_PRIORITY);

                final IToken<?> requestToken =
                  manager.createRequest(this,
                    delivery);

                deliveries.add(manager.getRequestForToken(requestToken));
            });

            return deliveries;
        }
        return null;
    }

    private void removeRequestFromTaskList(@NotNull final IRequest<? extends PublicCrafting> completedRequest, final IColony colony)
    {
        final ICitizenData holdingCrafter = colony.getCitizenManager().getCitizens()
                                              .stream()
                                              .filter(c -> c.getJob() instanceof AbstractJobCrafter && (
                                                ((AbstractJobCrafter) c.getJob()).getTaskQueue().contains(completedRequest.getId())
                                                  || ((AbstractJobCrafter) c.getJob()).getAssignedTasks().contains(completedRequest.getId())))
                                              .findFirst()
                                              .orElse(null);

        if (holdingCrafter != null)
        {
            final AbstractJobCrafter job = (AbstractJobCrafter) holdingCrafter.getJob();
            job.onTaskDeletion(completedRequest.getId());
        }
    }

    @NotNull
    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Nice!
    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @Override
    protected boolean canBuildingCraftStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuildingWorker building, @NotNull final ItemStack stack)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return false;
        }

        //Check if we even have a worker available
        return building.getAssignedCitizen()
                 .stream()
                 .anyMatch(c -> c.getJob() instanceof AbstractJobCrafter);
    }

    @Override
    protected void onAssignedToThisResolverForBuilding(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<? extends PublicCrafting> request,
      final boolean simulation,
      @NotNull final AbstractBuilding building)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return;
        }

        final ICitizenData freeCrafter = building.getAssignedCitizen()
                                           .stream()
                                           .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                           .min(Comparator.comparing((ICitizenData c) -> ((AbstractJobCrafter) c.getJob()).getTaskQueue().size() + ((AbstractJobCrafter) c.getJob())
                                                                                                                                                     .getAssignedTasks()
                                                                                                                                                     .size()))
                                           .orElse(null);

        if (freeCrafter == null)
        {
            onAssignedRequestBeingCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter job = (AbstractJobCrafter) freeCrafter.getJob();
        job.onTaskBeingScheduled(request.getId());
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request, @NotNull final AbstractBuilding building)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return;
        }

        final ICitizenData freeCrafter = building.getAssignedCitizen()
                                           .stream()
                                           .filter(c -> c.getJob() instanceof AbstractJobCrafter && ((AbstractJobCrafter) c.getJob()).getAssignedTasks().contains(request.getId()))
                                           .findFirst()
                                           .orElse(null);

        if (freeCrafter == null)
        {
            onAssignedRequestBeingCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter job = (AbstractJobCrafter) freeCrafter.getJob();
        job.onTaskBeingResolved(request.getId());
    }
}
