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
     * Method used to get the location that a delivery has to be brought to.
     * Usually this points for a Building to its Postbox.
     *
     * @return The location of the targetpoint of a delivery to this location.
     */
    @NotNull
    default ILocation getDeliveryLocation()
    {
        return getLocation();
    }

    /**
     * Method to get the location of this locatable.
     */
    @NotNull
    ILocation getLocation();

    /**
     * Method called by the request system to notify this requester that a
     */
    @NotNull
    void onRequestComplete(@NotNull final IToken token);
}
