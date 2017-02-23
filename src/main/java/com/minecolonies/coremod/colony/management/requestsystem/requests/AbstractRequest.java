package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by marcf on 2/23/2017.
 */
public abstract class AbstractRequest<T> implements IRequest<T> {

    @NotNull
    protected final IRequestToken token;
    @NotNull
    protected final IColony colony;
    @NotNull
    private RequestState state = RequestState.CREATED;
    @Nullable
    private IRequestResult<T> result;

    public AbstractRequest(@NotNull IColony colony, @NotNull IRequestToken token) {
        this.colony = colony;
        this.token = token;
    }

    /**
     * The unique token representing the request outside of the management system.
     *
     * @return the token representing the request outside of the management system.
     */
    @Override
    public IRequestToken getToken() {
        return token;
    }

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    @Override
    public RequestState getState() {
        return state;
    }

    /**
     * Setter for the current state of this request.
     *
     * @param state The new state of this request.
     */
    @Override
    public void setState(@NotNull RequestState state) {
        this.state = state;
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public IRequestResult<T> getResult() {
        return result;
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull IRequestResult<T> result) {
        this.result = result;
    }

    /**
     * Returns the colony this request was made in.
     *
     * @return the colony this request was made in.
     */
    @NotNull
    @Override
    public IColony getColony() {
        return colony;
    }
}
