package com.minecolonies.api.colony.requestsystem.requester;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.token.IToken;
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
    IToken getID();

    /**
     * Method to get the location of this locatable.
     * @return
     */
    @NotNull
    ILocation getLocation();

    /**
     * Method called by the request system to notify this requester that a
     */
    @NotNull
    void onRequestComplete(@NotNull final IToken token);
}
