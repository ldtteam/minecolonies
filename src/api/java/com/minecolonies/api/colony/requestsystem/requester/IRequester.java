package com.minecolonies.api.colony.requestsystem.requester;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Interface that describes an object that can be located in the Minecraft universe and can request objects inside a colony.
 */
public interface IRequester
{
    /**
     * Method to get the ID of a given requester.
     *
     * @return The id of this requester.
     */
    IToken getId();

    /**
     * Method to get the location of this locatable.
     *
     * @return the location.
     */
    @NotNull
    ILocation getLocation();

    /**
     * Method called by the request system to notify this requester that a request is complete.
     *
     * @param request the request.
     */
    @NotNull
    void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);

    /**
     * Method called by the request system to notify this requester that a request has been overruled.
     *
     * @param request the request.
     */
    @NotNull
    void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);

    /**
     * Gets the name of the requester that requested the request given by the token.
     *
     * @param request the request for which the name of the requester is retrieved
     * @return The display name of the requester.
     */
    @NotNull
    ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request);
}