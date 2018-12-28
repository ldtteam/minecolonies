package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.RequestHandler;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the crafter building.
 */
public abstract class AbstractBuildingCrafter extends AbstractBuildingWorker
{
    /**
     * Extra amount of recipes the crafters can learn.
     */
    private static final int EXTRA_RECIPE_MULTIPLIER = 10;

    /**
     * Instantiates a new crafter building.
     *
     * @param c the colony.
     * @param l the location
     */
    public AbstractBuildingCrafter(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableCollection<IRequestResolver<?>> supers = super.createResolvers();
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getRequesterLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PublicWorkerCraftingProductionResolver(getRequester().getRequesterLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    @Override
    public boolean buildingRequiresItemForCrafting(final ItemStack stack, final List<ItemStorage> localAlreadyKep, final boolean inventory)
    {
        final List<StandardRequests.AbstractCraftingRequest<?>> craftingRequests = getResolvers()
                                                                                     .stream()
                                                                                     .filter(iRequestResolver -> iRequestResolver instanceof AbstractCraftingRequestResolver)
                                                                                     .flatMap(iRequestResolver -> RequestHandler.getRequestsMadeByRequester(((IStandardRequestManager) getColony().getRequestManager()), iRequestResolver).stream())
                                                                                     .filter(iRequest -> iRequest instanceof StandardRequests.AbstractCraftingRequest)
                                                                                     .map(iRequest -> (StandardRequests.AbstractCraftingRequest<?>) iRequest)
                                                                                     .collect(Collectors.toList());

        final List<IRequest<? extends IDeliverable>> requirementRequests = getResolvers()
                                                                             .stream()
                                                                             .filter(iRequestResolver -> iRequestResolver instanceof AbstractCraftingProductionResolver)
                                                                             .flatMap(iRequestResolver -> RequestHandler.getRequestsMadeByRequester(((IStandardRequestManager) getColony().getRequestManager()), iRequestResolver).stream())
                                                                             .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                                                                             .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                                                                             .collect(Collectors.toList());

        for (final StandardRequests.AbstractCraftingRequest<?> craftingRequest : craftingRequests)
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(craftingRequest.getRequest().getStack(), stack) ||
                    ItemStackUtils.compareItemStackListIgnoreStackSize(craftingRequest.getDeliveries(), stack))
            {
                return true;
            }
        }

        for (final IRequest<? extends IDeliverable> requirement :
            requirementRequests)
        {
            if (requirement.getRequest().matches(stack) ||
                    ItemStackUtils.compareItemStackListIgnoreStackSize(requirement.getDeliveries(), stack))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overruleNextOpenRequestOfCitizenWithStack(
      @NotNull final CitizenData citizenData, @NotNull final ItemStack stack)
    {
        return super.overruleNextOpenRequestOfCitizenWithStack(citizenData, stack);
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        return AbstractBuildingCrafter.canBuildingCanLearnMoreRecipes (getBuildingLevel(), super.getRecipes().size());
    }

    /**
     * Crafter building View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiate the deliveryman view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Check if an additional recipe can be added.
         * @return true if so.
         */
        public boolean canRecipeBeAdded()
        {
            return AbstractBuildingCrafter.canBuildingCanLearnMoreRecipes (getBuildingLevel(), super.getRecipes().size());
        }
    }

    /**
     * Check if an additional recipe can be added.
     * @param learnedRecipes the learned recipes.
     * @param buildingLevel the building level.
     * @return true if so.
     */
    public static boolean canBuildingCanLearnMoreRecipes(final int buildingLevel, final int learnedRecipes)
    {
        return (Math.pow(2, buildingLevel) * EXTRA_RECIPE_MULTIPLIER) >= (learnedRecipes + 1);
    }
}
