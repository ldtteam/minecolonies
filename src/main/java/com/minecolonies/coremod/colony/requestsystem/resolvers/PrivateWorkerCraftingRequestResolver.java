package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
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

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 2x2 crafts which are crafted by the requesting worker.
 */
public class PrivateWorkerCraftingRequestResolver extends AbstractRequestResolver<IDeliverable>
{
    public PrivateWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
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
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
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
            final List<IToken<?>> tokens = new ArrayList<>();
            if(storage == null)
            {
                return null;
            }

            //todo After simulation has been added, we need to simulate the subrequest and decided wheter to try to resolve it or not.
            for(final ItemStack neededStack: storage.getInput())
            {
                final Stack stackRequest = new Stack(neededStack);
                tokens.add(manager.createRequest(this, stackRequest));
            }

            return tokens;
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
    public IRequest<?> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
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
