package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 3x3 crafts which are crafted by a crafter worker.
 */
public class PublicWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    /**
     * Initializing constructor.
     * @param location the location of the resolver.
     * @param token its id.
     */
    public PublicWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token, true);
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final List<IToken<?>> result = super.attemptResolve(manager, request);

        if (result == null)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        //We can do an instant get here, since we are already filtering on anything that has no entity.
        final CitizenData freeCrafter = colony.getCitizenManager()
                                              .getCitizens()
                                              .stream()
                                              .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                              .min(Comparator.comparing((CitizenData c) -> ((AbstractJobCrafter) c.getJob()).getTaskQueue().size()))
                                              .orElse(null);

        if (freeCrafter == null)
        {
            return null;
        }

        return result;
    }

    @Override
    public boolean canBuildingCraftStack(@NotNull final AbstractBuildingWorker building, final ItemStack stack)
    {
        return building.getFirstRecipe(stack) != null;
    }

    @Nullable
    @Override
    public IRequest<?> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> completedRequest)
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony)
        {
            final IRequestResolver resolver = colony.getRequestManager().getResolverForRequest(completedRequest.getToken());
            if(resolver instanceof PublicWorkerCraftingRequestResolver)
            {
                final Delivery delivery = new Delivery(resolver.getRequesterLocation(), completedRequest.getRequester().getRequesterLocation(), completedRequest.getDelivery().copy());

                final IToken<?> requestToken =
                        manager.createRequest(this,
                                delivery);
                return manager.getRequestForToken(requestToken);
            }
        }
        return null;
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return super.getRequesterId();
    }

    @Override
    public void onRequestBeingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request)
    {
        onRequestCancelled(manager, request);
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        /*
         * Nothing to be done.
         */
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //NOOP
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Colony colony = (Colony) manager.getColony();
            final CitizenData holdingCrafter = colony.getCitizenManager().getCitizens()
                                                  .stream()
                                                  .filter(c -> c.getJob() instanceof AbstractJobCrafter && ((AbstractJobCrafter) c.getJob()).getTaskQueue().contains(request.getToken()))
                                                  .findFirst()
                                                  .orElse(null);

            if (holdingCrafter == null)
            {
                MineColonies.getLogger().error("Parent cancellation failed! Unknown request: " + request.getToken());
            }
            else
            {
                final AbstractJobCrafter job = (AbstractJobCrafter) holdingCrafter.getJob();
                job.onTaskDeletion(request.getToken());
            }
        }

        return null;
    }

    @Override
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request, @NotNull final AbstractBuilding building)
    {
        final Colony colony = (Colony) manager.getColony();
        //We can do an instant get here, since we are already filtering on anything that has no entity.
        final CitizenData freeCrafter = building.getAssignedCitizen()
                                          .stream()
                                          .filter(c -> c.getJob() instanceof AbstractJobCrafter)
                                          .min(Comparator.comparing((CitizenData c) -> ((AbstractJobCrafter) c.getJob()).getTaskQueue().size())
                                                 .thenComparing(Comparator.comparing(c -> {
                                                     BlockPos targetPos = request.getRequester().getRequesterLocation().getInDimensionLocation();
                                                     //We can do an instant get here, since we are already filtering on anything that has no entity.
                                                     BlockPos entityLocation = c.getCitizenEntity().get().getLocation().getInDimensionLocation();

                                                     return BlockPosUtil.getDistanceSquared(targetPos, entityLocation);
                                                 })))
                                          .orElse(null);

        if (freeCrafter == null)
        {
            onRequestCancelled(manager, request);
            return;
        }

        final AbstractJobCrafter job = (AbstractJobCrafter) freeCrafter.getJob();
        job.addRequest(request.getToken());
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        IRequest<?> request = manager.getRequestForToken(token);

        if (request == null)
        {
            return new TextComponentString("<UNKNOWN>");
        }

        return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_PUBLIC_CRAFTING_RESOLVER_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }
}
