package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractCraftingProductionResolver;
import net.minecraft.util.text.ITextComponent;
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
        super(token, location, TypeConstants.PRIVATE_CRAFTING);
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
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PrivateCrafting> request)
    {
        return null;
    }

    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends PrivateCrafting> request)
    {

    }

    @NotNull
    @Override
    public void onRequestedRequestCompleted(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {

    }

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        final IRequest<?> cancelledRequest = manager.getRequestForToken(token);

    }

    /**
     * Method called by the request system to notify this requester that a request is complete.
     * Is also called by the request system, when a request has been overruled, and as such
     * completed by the player, instead of the initially assigned resolver.
     *
     * @param manager The request manager that has completed the given request.
     * @param request The request that has been completed.
     */
    @NotNull
    @Override
    public void onRequestedRequestCompleted(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    /**
     * Method called by the request system to notify this requester that a request has been cancelled.
     *
     * @param manager The request manager that has cancelled the given request.
     * @param request The request that has been cancelled.
     */
    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (request != null && request.hasParent())
        {
            final IRequest<?> parentRequest = manager.getRequestForToken(request.getParent());
            if (parentRequest.getState() != RequestState.CANCELLED && parentRequest.getState() != RequestState.OVERRULED)
            {
                manager.updateRequestState(request.getParent(), RequestState.CANCELLED);
            }
        }
    }

    /**
     * Gets the display name of the requester that requested the request.
     *
     * @param manager The request manager that wants to know what the display name of the requester for a given request is.
     * @param request The request for which the display name is being requested.
     * @return The display name of the requester.
     */
    @NotNull
    @Override
    public ITextComponent getDisplayName(
      @NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return null;
    }
}
