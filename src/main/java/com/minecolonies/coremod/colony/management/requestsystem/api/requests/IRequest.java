package com.minecolonies.coremod.colony.management.requestsystem.api.requests;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to represent requests, of type R, made to the internal market of the colony.
 * @param <R> The type of request, eg ItemStack, FluidStack etc.
 */
public interface IRequest<R> {

    /**
     * The unique token representing the request outside of the management system.
     * @return the token representing the request outside of the management system.
     */
    IRequestToken getToken();

    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     * @return The class that represents this Type of Request.
     */
    @NotNull
    Class<? extends R> getRequestType();

    /**
     * Returns the current state of the request.
     * @return The current state.
     */
    @NotNull
    RequestState getState();

    /**
     * Setter for the current state of this request.
     * @param state The new state of this request.
     */
    void setState(@NotNull RequestState state);

    /**
     * Return the object that is actually requested.
     * A RequestResolver can compare this object however way it sees fit.
     *
     * During the resolving process this object is called multiple times. But at least twice.
     * A cached implementation is preferred.
     * @return The object that is actually requested.
     */
    @NotNull
    R getRequest();

    /**
     * Returns the result of this request.
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    IRequestResult<R> getResult();

    /**
     * Setter for the result of the request.
     * @param result The new result of this request.
     */
    void setResult(@NotNull IRequestResult<R> result);

    /**
     * Returns the colony this request was made in.
     * @return the colony this request was made in.
     */
    @NotNull
    IColony getColony();

}
