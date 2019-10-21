package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrivateWorkerCraftingProductionResolver extends AbstractCraftingProductionResolver<PrivateCrafting>
{
    /**
     * Constructor to initialize.
     *
     * @param location the location.
     * @param token    the id.
     */
    public PrivateWorkerCraftingProductionResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token, PrivateCrafting.class);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PrivateCrafting> completedRequest)
    {
        return null;
    }

    @Nullable
    @Override
    public void onAssignedRequestBeingCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PrivateCrafting> request)
    {
        return;
    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PrivateCrafting> request)
    {

    }

    @NotNull
    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }
}
