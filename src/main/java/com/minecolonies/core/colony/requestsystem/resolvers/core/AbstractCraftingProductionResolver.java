package com.minecolonies.core.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.requestsystem.requesters.IBuildingBasedRequester;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract crafting resolver for all crafting tasks.
 */
public abstract class AbstractCraftingProductionResolver<C extends AbstractCrafting> extends AbstractRequestResolver<C> implements IBuildingBasedRequester
{
    private final Class<C> cClass;
    private final JobEntry jobEntry;

    /**
     * Constructor to initialize.
     *
     * @param location the location.
     * @param token    the id.
     * @param cClass   the class.
     */
    public AbstractCraftingProductionResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token,
      @NotNull final JobEntry jobEntry, final Class<C> cClass)
    {
        super(location, token);
        this.cClass = cClass;
        this.jobEntry = jobEntry;
    }

    @Override
    public TypeToken<? extends C> getRequestType()
    {
        return TypeToken.of(cClass);
    }

    /**
     * Get the job entry for the production resolver.
     * @return the entry.
     */
    public JobEntry getJobEntry()
    {
        return this.jobEntry;
    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends C> requestToCheck)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            final ILocation requesterLocation = requestToCheck.getRequester().getLocation();
            return requesterLocation.equals(getLocation());
        }

        return false;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        return attemptResolveForBuilding(manager, request, building);
    }

    @Nullable
    public List<IToken<?>> attemptResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final AbstractBuilding building)
    {
        final AbstractBuilding buildingWorker = (AbstractBuilding) building;
        return attemptResolveForBuildingAndStack(
          manager,
          buildingWorker,
          request.getRequest().getStack(),
          request.getRequest().getCount(),
          request.getRequest().getMinCount(),
          request.getRequest().getRecipeID());
    }

    @Nullable
    protected List<IToken<?>> attemptResolveForBuildingAndStack(
      @NotNull final IRequestManager manager,
      @NotNull final AbstractBuilding building,
      final ItemStack stack,
      final int count,
      final int minCount,
      final IToken<?> recipeId)
    {
        final ICraftingBuildingModule module = building.getCraftingModuleForRecipe(recipeId);
        if (module == null)
        {
            return null;
        }

        if (!canBuildingCraftStack(manager, building, stack))
        {
            return null;
        }

        final IRecipeStorage fullfillableCrafting = module.getFirstFulfillableRecipe(itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack), count, true);
        if (fullfillableCrafting != null)
        {
            return ImmutableList.of();
        }

        final IRecipeStorage craftableCrafting = module.getFirstRecipe(stack);
        if (craftableCrafting == null)
        {
            return null;
        }

        return createRequestsForRecipe(manager, building, count, minCount, craftableCrafting);
    }

    protected boolean canBuildingCraftStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuilding building, @NotNull final ItemStack stack)
    {
        return true;
    }

    @Nullable
    protected List<IToken<?>> createRequestsForRecipe(
      @NotNull final IRequestManager manager,
      @NotNull final AbstractBuilding building,
      final int count,
      final int minCount,
      @NotNull final IRecipeStorage storage)
    {
        final List<IToken<?>> materialRequests = new ArrayList<>();
        for (final ItemStorage ingredient : storage.getCleanedInput())
        {
            if (!ItemStackUtils.isEmpty(ingredient.getItemStack()))
            {
                final ItemStack craftingHelperStack = ingredient.getItemStack().copy();
                final ItemStack container = ingredient.getItemStack().getCraftingRemainingItem();
                //if recipe secondary produces craftinghelperstack, don't add it by count, add it once. If it's in the tools list, check to see if we need it first. 
                if(!storage.getSecondaryOutputs().isEmpty() && ItemStackUtils.compareItemStackListIgnoreStackSize(storage.getSecondaryOutputs(), craftingHelperStack, false, true))
                {
                    materialRequests.add(createNewRequestForStack(manager, craftingHelperStack, ingredient.getAmount(), ingredient.getAmount(), false));
                }
                else if(!storage.getCraftingTools().isEmpty() && ItemStackUtils.compareItemStackListIgnoreStackSize(storage.getCraftingTools(), craftingHelperStack, false, true))
                {
                    int requiredForDurability = craftingHelperStack.isDamageableItem() ? (int) Math.ceil((double) count / ingredient.getRemainingDurablityValue()) : ingredient.getAmount();
                    materialRequests.add(createNewRequestForStack(manager, craftingHelperStack, requiredForDurability, requiredForDurability, false));
                }
                else if (!ItemStackUtils.isEmpty(container) && ItemStackUtils.compareItemStacksIgnoreStackSize(container, craftingHelperStack, false, true))
                {
                    materialRequests.add(createNewRequestForStack(manager, craftingHelperStack, ingredient.getAmount(), ingredient.getAmount(), false));
                } 
                else
                {
                    materialRequests.add(createNewRequestForStack(manager, craftingHelperStack, ingredient.getAmount() * count, ingredient.getAmount() * minCount, true ));
                }
            }
        }
        return materialRequests;
    }

    @Nullable
    protected IToken<?> createNewRequestForStack(@NotNull final IRequestManager manager, final ItemStack stack, final int count, final int minCount, final boolean matchMeta)
    {
        final Stack stackRequest = new Stack(stack, matchMeta, true, ItemStack.EMPTY, count, minCount);
        return manager.createRequest(this, stackRequest);
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        resolveForBuilding(manager, request, building);
    }

    protected void onAssignedToThisResolverForBuilding(
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<? extends C> request,
      final boolean simulation,
      @NotNull final AbstractBuilding building)
    {
        //Noop
    }

    /**
     * Called by the manager given to indicate that this request has been assigned to you.
     *
     * @param manager    The systems manager.
     * @param request    The request assigned.
     * @param simulation True when simulating.
     */
    @Override
    public void onRequestAssigned(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, final boolean simulation)
    {
        final AbstractBuilding building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r).get();
        onAssignedToThisResolverForBuilding(manager, request, simulation, building);
    }

    /**
     * Resolve the request in a building.
     *
     * @param manager  the request manager.
     * @param request  the request.
     * @param building the building.
     */
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final AbstractBuilding building)
    {
        final AbstractBuilding buildingWorker = (AbstractBuilding) building;
        final ICraftingBuildingModule module = buildingWorker.getCraftingModuleForRecipe(request.getId());
        if (module == null)
        {
            manager.updateRequestState(request.getId(), RequestState.FAILED);
            return;
        }
        final IRecipeStorage storage = module.getFirstFulfillableRecipe(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, request.getRequest().getStack()), request.getRequest().getCount(), false);

        if (storage == null)
        {
            manager.updateRequestState(request.getId(), RequestState.FAILED);
            return;
        }

        final int craftingCount = CraftingUtils.calculateMaxCraftingCount(request.getRequest().getCount(), storage);
        for (int i = 0; i < craftingCount; i++)
        {
            module.fullFillRecipe(storage);
        }

        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
    }

    @Override
    public boolean isValid()
    {
        return jobEntry != null;
    }
}
