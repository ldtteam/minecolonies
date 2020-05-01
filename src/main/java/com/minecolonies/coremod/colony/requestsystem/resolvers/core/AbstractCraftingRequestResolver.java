package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.MAX_CRAFTING_CYCLE_DEPTH;

/**
 * Abstract crafting resolver for all crafting tasks.
 */
public abstract class AbstractCraftingRequestResolver extends AbstractRequestResolver<IDeliverable> implements IBuildingBasedRequester
{
    /**
     * Variable to check if requests can be requested externally.
     */
    public final boolean isPublicCrafter;

    /**
     * Constructor to initialize.
     * @param location the location.
     * @param token the id.
     * @param isPublicCrafter if public crafter or not.
     */
    public AbstractCraftingRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token, final boolean isPublicCrafter)
    {
        super(location, token);
        this.isPublicCrafter = isPublicCrafter;
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeToken.of(IDeliverable.class);
    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
	//Noop
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final ILocation requesterLocation = requestToCheck.getRequester().getLocation();
            if (isPublicCrafter || requesterLocation.equals(getLocation()))
            {
                final Optional<AbstractBuilding> building = getBuilding(manager, requestToCheck.getId()).map(r -> (AbstractBuilding) r);
                return building.map(b -> canResolveForBuilding(manager, requestToCheck, b)).orElse(false);
            }
        }

        return false;
    }

    /**
     * Check if the request can be resolved for this building.
     * @param manager the manager.
     * @param request the request to check.
     * @param building the building to check.
     * @return true if so.
     */
    public boolean canResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        if (createsCraftingCycle(manager, request, request))
        {
            return false;
        }

        return building instanceof AbstractBuildingWorker && canBuildingCraftStack((AbstractBuildingWorker) building, itemStack -> request.getRequest().matches(itemStack));
    }

    /**
     * Method to check if a crafting cycle can be created.
     *
     * @param manager the manager.
     * @param request the request.
     * @param target the target.
     * @return true if so.
     */
    protected boolean createsCraftingCycle(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<?> request,
      @NotNull final IRequest<? extends IDeliverable> target)
    {
        return createsCraftingCycle(manager, request, target, 0, new ArrayList<>());
    }

    /**
     * Method to check if a crafting cycle can be created.
     *
     * @param manager the manager.
     * @param request the request.
     * @param target the target to create.
     * @param count the itemCount.
     * @param reqs the list of reqs.
     * @return true if possible.
     */
    protected boolean createsCraftingCycle(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<?> request,
      @NotNull final IRequest<? extends IDeliverable> target,
      final int count,
      final List<IRequestable> reqs)
    {
        if (reqs.contains(request.getRequest()))
        {
            for (final IRequestable requestable : reqs)
            {
                if (requestable.equals(request.getRequest())
                      && request.getRequest() instanceof IDeliverable
                      && requestable instanceof IDeliverable
                      && ((IDeliverable) request.getRequest()).getCount() < ((IDeliverable) requestable).getCount() )
                {
                    return true;
                }
            }
        }
        reqs.add(request.getRequest());

        if (count > MAX_CRAFTING_CYCLE_DEPTH)
        {
            return true;
        }

        if (!request.equals(target) && request.getRequest().equals(target.getRequest()))
        {
            if (request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).getCount() < target.getRequest().getCount())
            {
                return true;
            }
        }

        if (!request.hasParent())
        {
            return false;
        }

        return createsCraftingCycle(manager, manager.getRequestForToken(request.getParent()), target, count+1, reqs);
    }

    /**
     * Check if a building can craft a certain stack.
     * @param building the building to check in.
     * @param stackPredicate predicate used to check if a building knows the recipe.
     * @return true if so.
     */
    public abstract boolean canBuildingCraftStack(@NotNull final AbstractBuildingWorker building, Predicate<ItemStack> stackPredicate);

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        return attemptResolveForBuilding(manager, request, building);
    }

    @Nullable
    public List<IToken<?>> attemptResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        final AbstractBuildingWorker buildingWorker = (AbstractBuildingWorker) building;
        return attemptResolveForBuildingAndStack(manager, buildingWorker, itemStack -> request.getRequest().matches(itemStack), request.getRequest().getCount());
    }

    @Nullable
    protected List<IToken<?>> attemptResolveForBuildingAndStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuildingWorker building, @NotNull final Predicate<ItemStack> stackPrecicate, final int count)
    {
        final IRecipeStorage craftableCrafting = building.getFirstRecipe(stackPrecicate);
        if (craftableCrafting == null)
        {
            return null;
        }

        return createRequestsForRecipe(manager, craftableCrafting.getPrimaryOutput(), count);
    }

    @Nullable
    protected List<IToken<?>> createRequestsForRecipe(
            @NotNull final IRequestManager manager,
            final ItemStack requestStack,
            final int count)
    {
        final int recipeExecutionsCount = (int) Math.ceil( (double) count / requestStack.getCount());
        return ImmutableList.of(manager.createRequest(this, createNewRequestableForStack(requestStack.copy(), recipeExecutionsCount)));
    }

    protected abstract IRequestable createNewRequestableForStack(ItemStack stack, final int count);

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        resolveForBuilding(manager, request, building);
    }

    /**
     * Resolve the request in a building.
     * @param manager the request manager.
     * @param request the request.
     * @param building the building.
     */
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }


}
