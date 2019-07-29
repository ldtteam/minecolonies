package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> completedRequest)
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony || !completedRequest.hasParent())
        {
            //This is the crafting that got completed.
            //We go up the tree one level to get the actual request.
            //Get the requester for that request and ask where he wants his stuff delivered.
            final IRequest<?> parentRequest = manager.getRequestForToken(completedRequest.getParent());
            final IRequester parentRequestRequester = parentRequest.getRequester();

            final List<IRequest<?>> deliveries = Lists.newArrayList();

            completedRequest.getDeliveries().forEach(parentRequest::addDelivery);
            completedRequest.getDeliveries().forEach(itemStack -> {
                final Delivery delivery = new Delivery(getLocation(), parentRequestRequester.getDeliveryLocation(), itemStack);

                final IToken<?> requestToken =
                  manager.createRequest(this,
                    delivery);

                deliveries.add(manager.getRequestForToken(requestToken));
            });

            return deliveries;
        }
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            final ICitizenData holdingCrafter = colony.getCitizenManager().getCitizens()
                                                 .stream()
                                                 .filter(c -> c.getJob() instanceof AbstractJobCrafter && (((AbstractJobCrafter) c.getJob()).getTaskQueue().contains(request.getId()) || ((AbstractJobCrafter) c.getJob()).getAssignedTasks().contains(request.getId())))
                                                 .findFirst()
                                                 .orElse(null);

            if (holdingCrafter == null)
            {
                MineColonies.getLogger().error("Parent cancellation of crafting production failed! Unknown request: " + request.getId());
            }
            else
            {
                final AbstractJobCrafter job = (AbstractJobCrafter) holdingCrafter.getJob();
                job.onTaskDeletion(request.getId());
            }
        }

        return null;
    }

    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request)
    {
        this.onRequestCancelled(manager, request);
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //Nice!
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        this.onRequestCancelled(manager, (IRequest<? extends PublicCrafting>) Objects.requireNonNull(manager.getRequestForToken(token)));
    }

    @Override
    protected boolean canBuildingCraftStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuildingWorker building, @NotNull final ItemStack stack)
    {
        if (manager.getColony().getWorld().isRemote)
            return false;

        //Check if we even have a worker available
        return building.getAssignedCitizen()
                  .stream()
                  .anyMatch(c -> c.getJob() instanceof AbstractJobCrafter);
    }

    @Override
    protected void onAssignedToThisResolverForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request, final boolean simulation, @NotNull final AbstractBuilding building)
    {
        if (manager.getColony().getWorld().isRemote)
            return;

        final ICitizenData freeCrafter = building.getAssignedCitizen()
                                          .stream()
                                          .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                          .min(Comparator.comparing((ICitizenData c) -> ((AbstractJobCrafter) c.getJob()).getTaskQueue().size() + ((AbstractJobCrafter) c.getJob()).getAssignedTasks().size()))
                                          .orElse(null);

        if (freeCrafter == null)
        {
            onRequestCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter job = (AbstractJobCrafter) freeCrafter.getJob();
        job.onTaskBeingScheduled(request.getId());
    }

    @Override
    public void resolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> request, @NotNull final AbstractBuilding building)
    {
        if (manager.getColony().getWorld().isRemote)
            return;

        final ICitizenData freeCrafter = building.getAssignedCitizen()
                                             .stream()
                                             .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                             .filter(c -> ((AbstractJobCrafter) c.getJob()).getAssignedTasks().contains(request.getId()))
                                             .findFirst()
                                             .orElse(null);

        if (freeCrafter == null)
        {
            onRequestCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter job = (AbstractJobCrafter) freeCrafter.getJob();
        job.onTaskBeingResolved(request.getId());
    }
}
