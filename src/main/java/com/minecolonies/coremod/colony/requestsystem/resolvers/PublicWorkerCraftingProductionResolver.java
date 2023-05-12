package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getDefaultDeliveryPriority;

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
      @NotNull final IToken<?> token,
      @NotNull final JobEntry jobEntry)
    {
        super(location, token, jobEntry, PublicCrafting.class);
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {

    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {
        if (!manager.getColony().getWorld().isClientSide)
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

            parentRequest.addDelivery(completedRequest.getDeliveries());
            completedRequest.getDeliveries().forEach(itemStack -> {
                final Delivery delivery = new Delivery(getLocation(), parentRequestRequester.getLocation(), itemStack, getDefaultDeliveryPriority(true));

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
                                                ((AbstractJobCrafter<?, ?>) c.getJob()).getTaskQueue().contains(completedRequest.getId())
                                                  || ((AbstractJobCrafter<?, ?>) c.getJob()).getAssignedTasks().contains(completedRequest.getId())))
                                              .findFirst()
                                              .orElse(null);

        if (holdingCrafter != null)
        {
            final AbstractJobCrafter<?, ?> job = (AbstractJobCrafter<?, ?>) holdingCrafter.getJob();
            job.onTaskDeletion(completedRequest.getId());
        }
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Nice!
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final IRequester requester = manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation());
        if (requester instanceof IBuildingView)
        {
            final IBuildingView bwv = (IBuildingView) requester;
            return Component.translatable(bwv.getModuleViewMatching(WorkerBuildingModuleView.class, m -> m.getJobEntry() == getJobEntry()).getJobDisplayName());
        }
        if (requester instanceof IBuilding)
        {
            final IBuilding building = (IBuilding) requester;
            return Component.translatable(building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == getJobEntry()).getJobDisplayName());
        }
        return super.getRequesterDisplayName(manager, request);
    }

    @Override
    protected boolean canBuildingCraftStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuilding building, @NotNull final ItemStack stack)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return false;
        }

        //Check if we even have a worker available
        return building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == getJobEntry()).getAssignedCitizen()
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
        if (manager.getColony().getWorld().isClientSide)
        {
            return;
        }

        final ICitizenData freeCrafter = building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == getJobEntry()).getAssignedCitizen()
                                           .stream()
                                           .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                           .min(Comparator.comparing((ICitizenData c) -> ((AbstractJobCrafter<?, ?>) c.getJob()).getTaskQueue().size()
                                                                                           + ((AbstractJobCrafter<?, ?>) c.getJob())
                                                                                               .getAssignedTasks()
                                                                                               .size()))
                                           .orElse(null);

        if (freeCrafter == null)
        {
            onAssignedRequestBeingCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter<?, ?> job = (AbstractJobCrafter<?, ?>) freeCrafter.getJob();
        job.onTaskBeingScheduled(request.getId());
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request, @NotNull final AbstractBuilding building)
    {
        if (manager.getColony().getWorld().isClientSide)
        {
            return;
        }

        final ICitizenData freeCrafter = building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == getJobEntry()).getAssignedCitizen()
                                           .stream()
                                           .filter(c -> c.getJob() instanceof AbstractJobCrafter && ((AbstractJobCrafter<?, ?>) c.getJob()).getAssignedTasks()
                                                                                                      .contains(request.getId()))
                                           .findFirst()
                                           .orElse(null);

        if (freeCrafter == null)
        {
            onAssignedRequestBeingCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter<?, ?> job = (AbstractJobCrafter<?, ?>) freeCrafter.getJob();
        job.onTaskBeingResolved(request.getId());
    }
}
