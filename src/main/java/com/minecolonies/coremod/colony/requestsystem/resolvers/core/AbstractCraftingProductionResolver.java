package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
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
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.MAX_CRAFTING_CYCLE_DEPTH;

/**
 * Abstract crafting resolver for all crafting tasks.
 */
public abstract class AbstractCraftingProductionResolver<C extends AbstractCrafting> extends AbstractRequestResolver<C> implements IBuildingBasedRequester
{
    private final Class<C> cClass;

    /**
     * Constructor to initialize.
     * @param location the location.
     * @param token the id.
     * @param cClass
     */
    public AbstractCraftingProductionResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token, final Class<C> cClass)
    {
        super(location, token);
        this.cClass = cClass;
    }

    @Override
    public TypeToken<? extends C> getRequestType()
    {
        return TypeToken.of(cClass);
    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            return Optional.ofNullable(manager.getColony().getRequesterBuildingForPosition(getRequesterLocation().getInDimensionLocation()));
        }

        return Optional.empty();
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends C> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            final ILocation requesterLocation = requestToCheck.getRequester().getRequesterLocation();
            return requesterLocation.equals(getRequesterLocation());
        }

        return false;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getToken()).map(r -> (AbstractBuilding) r).get();
        return attemptResolveForBuilding(manager, request, building);
    }

    @Nullable
    public List<IToken<?>> attemptResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final AbstractBuilding building)
    {
        final AbstractBuildingWorker buildingWorker = (AbstractBuildingWorker) building;
        return attemptResolveForBuildingAndStack(manager, buildingWorker, request.getRequest().getStack());
    }

    @Nullable
    protected List<IToken<?>> attemptResolveForBuildingAndStack(@NotNull final IRequestManager manager, @NotNull final AbstractBuildingWorker building, final ItemStack stack)
    {
        final IRecipeStorage fullfillableCrafting = building.getFirstFullFillableRecipe(stack);
        if (fullfillableCrafting != null)
        {
            return ImmutableList.of();
        }

        final IRecipeStorage craftableCrafting = building.getFirstRecipe(stack);
        if (craftableCrafting == null)
        {
            return null;
        }

        return createRequestsForRecipe(manager, building, stack, craftableCrafting);
    }

    /**
     * Calculate the max time a recipe has to be executed.
     * @param outputStack the output stack.
     * @param storage the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final ItemStack outputStack, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = (int)Math.ceil(Math.max(ItemStackUtils.getSize(outputStack), ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double)ItemStackUtils.getSize(storage.getPrimaryOutput()));

        //Now check if we excede an ingredients max stack size.
        for(final ItemStorage ingredientStorage : storage.getCleanedInput())
        {
            final ItemStack ingredient = ingredientStorage.getItemStack();
            //Calculate the input count for the ingredient.
            final int ingredientInputCount = ItemStackUtils.getSize(ingredient) * craftingCount;
            //Check if we are above the max stacksize.
            if (ingredientInputCount > ingredient.getMaxStackSize())
            {
                //Recalculate the crafting limit using the maxstacksize of the ingredient.
                craftingCount = Math.max(ingredient.getMaxStackSize(), ItemStackUtils.getSize(storage.getPrimaryOutput())) / ItemStackUtils.getSize(storage.getPrimaryOutput());
            }
        }

        return craftingCount;
    }

    @Nullable
    protected List<IToken<?>> createRequestsForRecipe(
            @NotNull final IRequestManager manager,
            @NotNull final AbstractBuildingWorker building,
            final ItemStack requestStack,
            @NotNull final IRecipeStorage storage)
    {
        final int craftingCount = calculateMaxCraftingCount(requestStack, storage);
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

    @Nullable
    protected IToken<?> createNewRequestForStack(@NotNull final IRequestManager manager, final ItemStack stack)
    {
        return manager.createRequest(this, new Stack(stack.copy()));
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request)
    {
        final AbstractBuilding building = getBuilding(manager, request.getToken()).map(r -> (AbstractBuilding) r).get();
        resolveForBuilding(manager, request, building);
    }

    /**
     * Resolve the request in a building.
     * @param manager the request manager.
     * @param request the request.
     * @param building the building.
     */
    public void resolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends C> request, @NotNull final AbstractBuilding building)
    {
        final AbstractBuildingWorker buildingWorker = (AbstractBuildingWorker) building;
        final IRecipeStorage storage = buildingWorker.getFirstFullFillableRecipe(request.getRequest().getStack());

        if (storage == null)
        {
            Log.getLogger().error("Failed to craft a crafting recipe of: " + request.getRequest().getStack().toString() + ". Its ingredients are missing.");
            return;
        }

        final int craftingCount = calculateMaxCraftingCount(request.getRequest().getStack(), storage);
        for (int i = 0; i < craftingCount; i++)
        {
            buildingWorker.fullFillRecipe(storage);
        }

        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
    }
}
