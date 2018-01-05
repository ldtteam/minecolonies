package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractRequestResolver;
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
public class PrivateWorkerCraftingRequestResolver extends AbstractCraftingRequestResolver
{
    public PrivateWorkerCraftingRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canBuildingCraftStack(@NotNull final AbstractBuildingWorker building, final ItemStack stack)
    {
        return building.getFirstRecipe(stack) != null;
    }

    @Nullable
    @Override
    public IRequest<?> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends Stack> request)
    {
        return null;
    }

    @Override
    public void onRequestComplete(@NotNull final IToken<?> token)
    {
        /**
         * Nothing to be done.
         */
    }

    @Override
    public void onRequestCancelled(@NotNull final IToken<?> token)
    {
        /**
         * Nothing to be done.
         */
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
