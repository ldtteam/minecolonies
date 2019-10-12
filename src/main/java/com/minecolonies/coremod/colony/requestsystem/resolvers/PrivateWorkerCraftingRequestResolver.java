package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.RSConstants.CONST_CRAFTING_RESOLVER_PRIORITY;

/**
 * A crafting resolver which takes care of 2x2 crafts which are crafted by the requesting worker.
 */
public class PrivateWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    public PrivateWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token, false);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> completedRequest)
    {
        //No followup needed, crafting already completed at the requesting building / worker.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        return null;
    }

    @Override
    public void onRequestBeingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request)
    {
        //NOOP
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        /**
         * Nothing to be done.
         */
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        //Noop
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        IRequest<?> request = manager.getRequestForToken(token);

        if (request == null)
        {
            return new TextComponentString("<UNKNOWN>");
        }

        if (request.hasParent())
        {
            request = manager.getRequestForToken(request.getParent());
        }
        else
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME);
        }

        if (request == null)
        {
            return new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME);
        }

        return request.getRequester().getDisplayName(manager, request.getId())
                 .appendSibling(new TextComponentString(" ("))
                 .appendSibling(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_PRIVATE_CRAFTING_RESOLVER_NAME))
                 .appendSibling(new TextComponentString(")"));
    }

    @Override
    public int getPriority()
    {
        return CONST_CRAFTING_RESOLVER_PRIORITY;
    }

    @Override
    public boolean canBuildingCraftStack(@NotNull final AbstractBuildingWorker building, final Predicate<ItemStack> stackPredicate)
    {
        return building.getFirstRecipe(stackPredicate) != null;
    }

    @Override
    protected IRequestable createNewRequestableForStack(final ItemStack stack, final int count)
    {
        return new PrivateCrafting(stack, count);
    }
}
