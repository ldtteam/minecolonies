package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.exceptions.NoBuildingWithAssignedRequestFoundException;
import com.minecolonies.coremod.colony.requestsystem.exceptions.ResolvedRequestIsNotDeliverable;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract crafting resolver for all crafting tasks.
 */
public abstract class AbstractCraftingProductionResolver<C extends AbstractCrafting> extends AbstractRequestResolver<C> implements IBuildingBasedRequester
{

    public AbstractCraftingProductionResolver(
      @NotNull final IToken<?> token,
      @NotNull final ILocation location,
      @NotNull final TypeToken<? extends C> requestType
    )
    {
        super(token, location, requestType);
    }

    @NotNull
    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }

    @NotNull
    public Optional<IBuildingWorker> getWorkerBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return this.getBuilding(manager, request)
                                                              .map(requester -> ((IBuilding) requester))
                                                              .filter(building -> building instanceof AbstractBuildingWorker)
                                                              .map(building -> ((IBuildingWorker) building));

    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends C> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final Optional<IBuildingWorker> buildingWorkerOptional = this.getWorkerBuilding(manager, requestToCheck);
            if (!buildingWorkerOptional.isPresent())
                return false;

            final IBuildingWorker iBuildingWorker = buildingWorkerOptional.get();
            return canResolveForBuilding(manager, requestToCheck, iBuildingWorker);
        }

        return false;
    }

    private boolean canResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final IBuildingWorker buildingWorker)
    {
        if (!(request.getRequest() instanceof IDeliverable))
            return false;
        final IDeliverable deliverable = (IDeliverable) request.getRequest();

        return buildingWorker.getFirstRecipe(deliverable::matches) != null;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final Optional<IBuildingWorker> buildingWorkerOptional = this.getWorkerBuilding(manager, request);
        if (!buildingWorkerOptional.isPresent())
            return null;

        final IBuildingWorker buildingWorker = buildingWorkerOptional.get();
        return attemptResolveForBuilding(manager, request, buildingWorker);
    }

    @Nullable
    private List<IToken<?>> attemptResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final IBuildingWorker buildingWorker)
    {
        if (!(request.getRequest() instanceof IDeliverable))
        {
            return null;
        }

        final IDeliverable deliverable = (IDeliverable) request.getRequest();

        if (!canBuildingCraftStack(buildingWorker, deliverable))
        {
            return null;
        }

        final IRecipeStorage fullfillableCrafting = buildingWorker.getFirstFullFillableRecipe(deliverable::matches);
        if (fullfillableCrafting != null)
        {
            return ImmutableList.of();
        }

        final IRecipeStorage craftableCrafting = buildingWorker.getFirstRecipe(deliverable::matches);
        if (craftableCrafting == null)
        {
            return null;
        }

        return createRequestsForRecipe(manager, deliverable, buildingWorker, craftableCrafting);
    }

    private boolean canBuildingCraftStack(
      @NotNull final IBuildingWorker buildingWorker,
      @NotNull final IDeliverable deliverable
    )
    {
        return buildingWorker.getFirstFullFillableRecipe(deliverable::matches) != null;
    }

    @NotNull
    private List<IToken<?>> createRequestsForRecipe(
      @NotNull final IRequestManager manager,
      @NotNull final IDeliverable deliverable,
      @NotNull final IBuildingWorker building,
      @NotNull final IRecipeStorage storage)
    {
        final int craftingCount = CraftingUtils.calculateMaxCraftingCount(deliverable.getCount(), storage);
        return storage.getCleanedInput().stream()
                 .filter(s -> !ItemStackUtils.isEmpty(s.getItemStack()))
                 .filter(s -> InventoryUtils.getItemCountInItemHandler(new InvWrapper(building.getMainCitizen().getInventory()),
                   stack -> !ItemStackUtils.isEmpty(stack) && s.getItemStack().isItemEqual(stack)) < s.getAmount())
                 .map(stack -> {
                     final ItemStack craftingHelperStack = stack.getItemStack().copy();
                     ItemStackUtils.setSize(craftingHelperStack, stack.getAmount() * craftingCount);

                     return createNewRequestForStack(manager, craftingHelperStack);
                 }).collect(Collectors.toList());
    }

    @NotNull
    private IToken<?> createNewRequestForStack(@NotNull final IRequestManager manager, final ItemStack stack)
    {
        return manager.createRequest(this, new Stack(stack.copy()));
    }

    /**
     * Called by the manager given to indicate that this request has been assigned to you.
     *
     * @param manager    The systems manager.
     * @param request    The request assigned.
     * @param simulation True when simulating.
     */
    @Override
    public void onAssignedToThisResolver(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, final boolean simulation)
    {
        final IBuilding building = getBuilding(manager, request.getId()).map(r -> (IBuilding) r).get();
        onAssignedToThisResolverForBuilding(manager, request, simulation, building);
    }

    protected void onAssignedToThisResolverForBuilding(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<? extends C> request,
      final boolean simulation,
      @NotNull final IBuilding building)
    {
        //Noop
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final Optional<IBuildingWorker> buildingWorkerOptional = getWorkerBuilding(manager, request);
        if (!buildingWorkerOptional.isPresent())
            throw new NoBuildingWithAssignedRequestFoundException("Can not find building from request: " + request.toString());

        final IBuildingWorker buildingWorker = buildingWorkerOptional.get();
        resolveForBuilding(manager, request, buildingWorker);
    }

    /**
     * Resolve the request in a building.
     *
     * @param manager  the request manager.
     * @param request  the request.
     * @param buildingWorker the building.
     */
    private void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final IBuildingWorker buildingWorker)
    {
        if (!(request.getRequest() instanceof IDeliverable))
            throw new ResolvedRequestIsNotDeliverable("The request: " + request.toString() + " is not deliverable");

        final IRecipeStorage storage = buildingWorker.getFirstFullFillableRecipe(request.getRequest().getStack());
        if (storage == null)
        {
            //Not sure if we should throw.....
            Log.getLogger().error("Failed to craft a crafting recipe of: " + request.getRequest().getStack().toString() + ". Its ingredients are missing.");
            return;
        }

        final int craftingCount = CraftingUtils.calculateMaxCraftingCount(request.getRequest().getCount(), storage);
        for (int i = 0; i < craftingCount; i++)
        {
            buildingWorker.fullFillRecipe(storage);
        }

        manager.updateRequestState(request.getId(), RequestState.COMPLETED);
    }
}
