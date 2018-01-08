package com.minecolonies.api.colony.requestsystem.requester;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
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
    IToken<?> getRequesterId();

    /**
     * Method used to get the location that a delivery has to be brought to.
     * Usually this points for a Building to its Postbox.
     *
     * @return The location of the targetpoint of a delivery to this location.
     */
    @NotNull
    default ILocation getDeliveryLocation()
    {
        return getRequesterLocation();
    }

    /**
     * Method to get the location of this locatable.
     *
     * @return the location.
     */
    @NotNull
    ILocation getRequesterLocation();

    /**
     * Method called by the request system to notify this requester that a request is complete.
     *
     * @param token the token of the request.
     */
    @NotNull
    void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token);

    /**
     * Method called by the request system to notify this requester that a request has been overruled.
     *
     * @param token The token of the request.
     */
    @NotNull
    void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token);

    /**
     * Gets the name of the requester that requested the request given by the token.
     *
     * @param token The token of the request for which the name of the requester is retrieved
     * @return The display name of the requester.
     */
    @NotNull
    ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token);
}
