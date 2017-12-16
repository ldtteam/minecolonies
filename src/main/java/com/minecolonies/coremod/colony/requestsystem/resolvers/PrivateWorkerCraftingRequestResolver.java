package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class PrivateWorkerCraftingRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    public PrivateWorkerCraftingRequestResolver(
                                     @NotNull final ILocation location,
                                     @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends IDeliverable> getRequestType()
    {
        return TypeToken.of(IDeliverable.class);
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> requestToCheck)
    {
        if (!manager.getColony().getWorld().isRemote)
        {
            Colony colony = (Colony) manager.getColony();
            final ILocation requesterLocation = requestToCheck.getRequester().getRequesterLocation();
            final AbstractBuilding building = colony.getBuilding(requesterLocation.getInDimensionLocation());
            if(building instanceof AbstractBuildingWorker)
            {
                final ItemStack stack = requestToCheck.getRequest().getResult();

                if (ItemStackUtils.isEmpty(stack))
                {
                    return false;
                }

                if (((AbstractBuildingWorker) building).canCraft(stack))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    @Override
    @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
    /**
     * Moving the curly braces really makes the code hard to read.
     */
    public List<IToken<?>> attemptResolve(
                                        @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        if (manager.getColony().getWorld().isRemote)
        {
            return null;
        }

        final Colony colony = (Colony) manager.getColony();
        final ILocation requesterLocation = request.getRequester().getRequesterLocation();
        final AbstractBuilding building = colony.getBuilding(requesterLocation.getInDimensionLocation());
        if(canResolve(manager, request) && building instanceof AbstractBuildingWorker)
        {
            final ItemStack stack = request.getRequest().getResult();
            final RecipeStorage storage = ((AbstractBuildingWorker) building).getFirstFullFillableRecipe(stack);

            if(storage == null)
            {
                //todo create a request to request the first possible resolving, ask Orion how to possibly request different resolving methods (to not annoy the player about this)
                return null;
            }
            return Lists.newArrayList();
        }

        return null;
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        final Colony colony = (Colony) manager.getColony();
        final ILocation requesterLocation = request.getRequester().getRequesterLocation();
        final AbstractBuilding building = colony.getBuilding(requesterLocation.getInDimensionLocation());
        final ItemStack stack = request.getRequest().getResult();

        final RecipeStorage storage = ((AbstractBuildingWorker) building).getFirstFullFillableRecipe(stack);

        if(storage == null)
        {
            return;
        }

        ((AbstractBuildingWorker) building).fullFillRecipe(storage);
    }

    @Nullable
    @Override
    public IRequest<?> getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        //todo clean up the requests we made.
        return null;
    }

    @Override
    public void onRequestComplete(@NotNull final IToken<?> token)
    {
    }

    @Override
    public void onRequestCancelled(@NotNull final IToken<?> token)
    {

    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IToken<?> token)
    {
        return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_CRAFTING_RESOLVER_NAME);
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }
}
