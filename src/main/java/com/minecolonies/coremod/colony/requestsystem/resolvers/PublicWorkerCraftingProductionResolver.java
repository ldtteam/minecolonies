package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

public class PublicWorkerCraftingProductionResolver extends AbstractCraftingProductionResolver<PublicCrafting>
{
    public PublicWorkerCraftingProductionResolver(
      @NotNull final IToken<?> token,
      @NotNull final ILocation location)
    {
        super(token, location, TypeConstants.PUBLIC_CRAFTING);
    }

    @NotNull
    @Override
    public void onRequestedRequestCompleted(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Noop
    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //We do not need to eliminate yet, since the request
        manager.updateRequestState(request.getParent(), RequestState.CANCELLED);
    }

    @Override
    public void onRequestAssignedBeingCancelledOrOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PublicCrafting> requestBeingCancelledOrOverruled)
    {
        eliminateRequestFromJobQueue(manager, requestBeingCancelledOrOverruled, true);
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return null;
    }

    /**
     * Method used to remove the request from the job queue of a citizen that it was assigned to.
     * @param manager The manager that is used to query the world and the colony.
     * @param request The request which needs to be eliminated
     * @param notifyOnFailureToFind Indicate if an error should be logged when no citizen was found while searching.
     */
    private void eliminateRequestFromJobQueue(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request, boolean notifyOnFailureToFind)
    {
        final IColony colony = manager.getColony();
        final ICitizenData holdingCrafter = colony.getCitizenManager().getCitizens()
                                              .stream()
                                              .filter(c -> c.getJob() instanceof AbstractJobCrafter && (((AbstractJobCrafter) c.getJob()).getTaskQueue().contains(request.getId()) || ((AbstractJobCrafter) c.getJob()).getAssignedTasks().contains(request.getId())))
                                              .findFirst()
                                              .orElse(null);

        if (holdingCrafter == null)
        {
            if (notifyOnFailureToFind)
            {
                MineColonies.getLogger().error("Parent cancellation of crafting production failed! Unknown request: " + request.getId());
            }
        }
        else
        {
            final AbstractJobCrafter job = (AbstractJobCrafter) holdingCrafter.getJob();
            job.onTaskDeletion(request.getId());
        }
    }
}
