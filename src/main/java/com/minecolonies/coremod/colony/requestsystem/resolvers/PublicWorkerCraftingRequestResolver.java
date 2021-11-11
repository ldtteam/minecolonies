package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 3x3 crafts which are crafted by a crafter worker.
 */
public class PublicWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    /**
     * Initializing constructor.
     *
     * @param location the location of the resolver.
     * @param token    its id.
     */
    public PublicWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token, final JobEntry entry)
    {
        super(location, token, entry, true);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //Noop. The production resolver already took care of that.
        return null;
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {

    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        /*
         * Nothing to be done.
         */
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //NOOP
    }

    @NotNull
    @Override
    public IFormattableTextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final IRequester requester = manager.getColony().getRequesterBuildingForPosition(getLocation().getInDimensionLocation());
        if (requester instanceof IBuildingView)
        {
            final CraftingModuleView moduleView = ((IBuildingView) requester).getModuleViewMatching(CraftingModuleView.class, m -> m.getJobEntry() == getJobEntry());
            if (moduleView != null)
            {
                return new TranslationTextComponent(moduleView.getJobEntry().getTranslationKey());
            }
        }
        return super.getRequesterDisplayName(manager, request);
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }

    @Override
    public boolean canBuildingCraftStack(@NotNull final AbstractBuilding building, final Predicate<ItemStack> stackPredicate)
    {
        for (final ICraftingBuildingModule module : building.getModules(ICraftingBuildingModule.class))
        {
            if (module.getFirstRecipe(stackPredicate) != null)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IRequestable createNewRequestableForStack(final ItemStack stack, final int count, final int minCount, final IToken<?> recipeStorage)
    {
        return new PublicCrafting(stack, count, recipeStorage);
    }
}
