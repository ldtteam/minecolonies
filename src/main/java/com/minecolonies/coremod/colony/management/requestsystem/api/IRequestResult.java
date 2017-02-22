package com.minecolonies.coremod.colony.management.requestsystem.api;

import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/22/2017.
 */
public interface IRequestResult<T> {

    /**
     * The request that created this result.
     * @return The request that created this result.
     */
    @NotNull
    IRequest<T> getRequest();

    /**
     * The result from the request
     * @return The result from the request.
     */
    @NotNull
    T getResult();
}
