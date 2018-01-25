package com.minecolonies.coremod.colony.requestsystem.resolvers.core;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCraftingRequestResolver extends AbstractBuildingDependentRequestResolver<Stack>
{
    public AbstractCraftingRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends Stack> getRequestType()
    {
        return TypeToken.of(Stack.class);
    }

    @Override
    public boolean canResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request, @NotNull final AbstractBuilding building)
    {
        if (createsCraftingCycle(manager, request, request))
        {
            return false;
        }

        return building instanceof AbstractBuildingWorker && canBuildingCraftStack((AbstractBuildingWorker) building, request.getRequest().getStack());
    }

    protected boolean createsCraftingCycle(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request, @NotNull final IRequest<? extends Stack> target)
    {
        if (!request.equals(target) && request.getRequest().equals(target.getRequest()))
        {
            return true;
        }

        if (!request.hasParent())
        {
            return false;
        }

        return createsCraftingCycle(manager, manager.getRequestForToken(request.getParent()), target);
    }

    public abstract boolean canBuildingCraftStack(@NotNull final AbstractBuildingWorker building, ItemStack stack);

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request, @NotNull final AbstractBuilding building)
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

    protected int calculateMaxCraftingCount(@NotNull final ItemStack outputStack, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = Math.max(ItemStackUtils.getSize(outputStack), ItemStackUtils.getSize(storage.getPrimaryOutput())) / ItemStackUtils.getSize(storage.getPrimaryOutput());

        //Now check if we excede an ingredients max stack size.
        for(final ItemStack ingredient : storage.getInput())
        {
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
        return storage.getInput().stream()
                 .filter(s -> !ItemStackUtils.isEmpty(s))
                 .map(stack -> {
                    final ItemStack craftingHelperStack = stack.copy();
                    ItemStackUtils.setSize(craftingHelperStack, ItemStackUtils.getSize(craftingHelperStack) * craftingCount);

                    return createNewRequestForStack(manager, craftingHelperStack);
                }).collect(Collectors.toList());
    }

    @Nullable
    protected IToken<?> createNewRequestForStack(@NotNull final IRequestManager manager, final ItemStack stack)
    {
        return manager.createRequest(this, new Stack(stack.copy()));
    }

    @Override
    public void resolveForBuilding(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request, @NotNull final AbstractBuilding building)
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
