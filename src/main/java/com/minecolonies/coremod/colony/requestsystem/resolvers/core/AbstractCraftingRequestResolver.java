package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.MAX_CRAFTING_CYCLE_DEPTH;
import static com.minecolonies.api.research.util.ResearchConstants.INV_SLOTS;

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
     *
     * @param location        the location.
     * @param token           the id.
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
     *
     * @param manager  the manager.
     * @param request  the request to check.
     * @param building the building to check.
     * @return true if so.
     */
    public boolean canResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        if (createsCraftingCycle(manager, request, request))
        {
            return false;
        }

        // As long as we're not resolving food, fast resolve
        if(!(request.getRequest() instanceof Food))
        {
            return building instanceof AbstractBuildingWorker && canBuildingCraftStack((AbstractBuildingWorker) building, itemStack -> request.getRequest().matches(itemStack));
        }

        // If this building is resolving a generic food request, then only allow it to resolve non-smeltables. 
        if(building instanceof AbstractBuildingWorker)
        {
            final IRecipeStorage recipe = ((AbstractBuildingWorker) building).getFirstRecipe(itemStack -> request.getRequest().matches(itemStack));
            if( recipe != null && recipe.getIntermediate() != Blocks.FURNACE)
            {
                return canBuildingCraftStack((AbstractBuildingWorker) building, itemStack -> request.getRequest().matches(itemStack));
            }
        }

        // It's both a Food request and smeltable
        return false;
    }

    /**
     * Method to check if a crafting cycle can be created.
     *
     * @param manager the manager.
     * @param request the request.
     * @param target  the target.
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
     * @param target  the target to create.
     * @param count   the itemCount.
     * @param reqs    the list of reqs.
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
                      && ((IDeliverable) request.getRequest()).getCount() < ((IDeliverable) requestable).getCount())
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

        return createsCraftingCycle(manager, manager.getRequestForToken(request.getParent()), target, count + 1, reqs);
    }

    /**
     * Check if a building can craft a certain stack.
     *
     * @param building       the building to check in.
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
    public List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<? extends IDeliverable> request,
      @NotNull final AbstractBuilding building)
    {
        final AbstractBuildingWorker buildingWorker = (AbstractBuildingWorker) building;
        return attemptResolveForBuildingAndStack(manager,
          buildingWorker,
          itemStack -> request.getRequest().matches(itemStack),
          request.getRequest().getCount(),
          request.getRequest().getMinimumCount());
    }

    @Nullable
    protected List<IToken<?>> attemptResolveForBuildingAndStack(
      @NotNull final IRequestManager manager,
      @NotNull final AbstractBuildingWorker building,
      @NotNull final Predicate<ItemStack> stackPrecicate,
      final int count,
      final int minCount)
    {
        final IRecipeStorage craftableCrafting = building.getFirstRecipe(stackPrecicate);
        if (craftableCrafting == null)
        {
            return null;
        }

        return createRequestsForRecipe(manager, craftableCrafting, count, minCount);
    }

    /**
     * Create the crafting request entries for the overall request
     * Will produce multiple, if the ingredients don't all fit in the crafters inventory. 
     * @param manager       request manager
     * @param recipeRequest requested recipe instance
     * @param count         count of item requested
     * @param minCount      minimum count required
     * @return List of crafting requests necessary to fulfill the overall requests
     */
    @Nullable
    protected List<IToken<?>> createRequestsForRecipe(
      @NotNull final IRequestManager manager,
      final IRecipeStorage recipeRequest,
      final int count,
      final int minCount)
    {
        final List<ItemStorage> inputs = recipeRequest.getCleanedInput();
        final ItemStack requestStack = recipeRequest.getPrimaryOutput();
        final AbstractResearchEffect<Double> researchEffect =  manager.getColony().getResearchManager().getResearchEffects().getEffect(INV_SLOTS, AbstractResearchEffect.class);
        final int extraSlots = researchEffect != null ? researchEffect.getEffect().intValue() : 0;         
        final int maxSlots = (27 + extraSlots) - (27 + extraSlots) % 8;  // retaining 1 slot per row for 'overhead'

        int recipeExecutionsCount = (int) Math.ceil((double) count / requestStack.getCount());
        int minRecipeExecutionsCount = (int) Math.ceil((double) minCount / requestStack.getCount());
        int batchSize = recipeExecutionsCount;
        int totalSlots = Integer.MAX_VALUE;

        //Calculate how many slots are needed, and figure out the maximum number of iterations we can load ingredients for into inventory
        while (totalSlots > maxSlots)
        {
            //Start with how much space needed for the output, it's a little naive, as it assumes we need full output and ingredients at the same time
            int stacksNeeded = (int) Math.ceil((double)(requestStack.getCount() * batchSize) / requestStack.getMaxStackSize());
            for(ItemStorage ingredient : inputs)
            {
                stacksNeeded += (int) Math.ceil((double)(ingredient.getAmount() * batchSize) / ingredient.getItemStack().getMaxStackSize());
            }
            if (stacksNeeded > maxSlots)
            {
                //We can't fit everything into inventory. Reduce the batch size by the ratio of what we calculated and what we have available. 
                batchSize = (int) Math.floor((double) batchSize * ((double) maxSlots / stacksNeeded));
            }
            totalSlots = Math.min(totalSlots, stacksNeeded);
        }

        //Create a crafting request for each batch needed to supply the full request
        List<IToken<?>> requests = new ArrayList<>();
        while(recipeExecutionsCount > 0)
        {
            requests.add(manager.createRequest(this, createNewRequestableForStack(requestStack.copy(), Math.min(batchSize, recipeExecutionsCount), Math.max(1, Math.min(batchSize, minRecipeExecutionsCount)))));
            recipeExecutionsCount -= batchSize;
            minRecipeExecutionsCount  = minRecipeExecutionsCount > batchSize ? minRecipeExecutionsCount - batchSize : 0; 
        }

        return ImmutableList.copyOf(requests);
    }

    /**
     * Create a new requestable for a stack.
     *
     * @param stack    the stack to request.
     * @param count    the count needed.
     * @param minCount the min count to fulfill.
     * @return the requestable.
     */
    protected abstract IRequestable createNewRequestableForStack(ItemStack stack, final int count, final int minCount);

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        resolveForBuilding(manager, request, building);
    }

    /**
     * Resolve the request in a building.
     *
     * @param manager  the request manager.
     * @param request  the request.
     * @param building the building.
     */
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }
}
