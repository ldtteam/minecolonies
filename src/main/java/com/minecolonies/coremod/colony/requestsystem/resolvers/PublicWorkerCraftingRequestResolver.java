package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PublicWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{

    public PublicWorkerCraftingRequestResolver(
      @NotNull final IToken<?> token,
      @NotNull final ILocation location)
    {
        super(token, location);
    }

    @Override
    protected boolean isPublic()
    {
        return true;
    }

    @Override
    public boolean canBuildingCraftStack(@NotNull final IBuildingWorker building, final Predicate<ItemStack> stackPredicate)
    {
        //Check if we even have a worker available
        return building.getAssignedCitizen()
                 .stream()
                 .anyMatch(c -> c.getJob() instanceof AbstractJobCrafter) && building.getFirstRecipe(stackPredicate) != null;
    }

    @Override
    protected IRequestable createNewRequestableForStack(final ItemStack stack, final int count)
    {
        return new PublicCrafting(stack, count);
    }

    @NotNull
    @Override
    public void onRequestedRequestCompleted(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //This is a noop here.
        //The manager will continue with the updating of the parent, once all childs have been completed.
    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Notify the manager, that because we failed, our parent will also fail, since we were a requirement for the parent.
        //We will always have a parent, because a crafting request can not (or better should not) be made alone.
        manager.updateRequestState(request.getParent(), RequestState.CANCELLED);
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_PUBLIC_CRAFTING_RESOLVER_NAME)
             .appendSibling( new TextComponentString("("))
             .appendSibling(getBuilding(manager, request).map(requester -> requester.getDisplayName(manager, request)).orElse(new TextComponentString( "UNKNOWN")))
             .appendSibling(new TextComponentString(")"));
    }
}
