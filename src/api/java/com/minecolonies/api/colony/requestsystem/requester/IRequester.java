package com.minecolonies.api.colony.requestsystem.requester;

import com.minecolonies.api.colony.requestsystem.location.ILocatable;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.MatchesPattern;

/**
 * Interface that describes objects that can request requests inside a request system, while also getting notifications about
 * request completion, and cancellation.
 */
public interface IRequester extends ILocatable
{
    /**
     * Method to get the ID of a given requester.
     *
     * @return The id of this requester.
     */
    @NotNull
    IToken<?> getId();

    /**
     * Method called by the request system to notify this requester that a request is complete.
     * Is also called by the request system, when a request has been overruled, and as such
     * completed by the player, instead of the initially assigned resolver.
     *
     * @param manager The request manager that has completed the given request.
     * @param request The request that has been completed.
     */
    @NotNull
    void onRequestedRequestCompleted(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);

    /**
     * Method called by the request system to notify this requester that a request has been cancelled.
     *
     * @param manager The request manager that has cancelled the given request.
     * @param request The request that has been cancelled.
     */
    @NotNull
    void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);

    /**
     * Gets the display name of the requester that requested the request.
     *
     * @param manager The request manager that wants to know what the display name of the requester for a given request is.
     * @param request The request for which the display name is being requested.
     * @return The display name of the requester.
     */
    @NotNull
    ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);
}
