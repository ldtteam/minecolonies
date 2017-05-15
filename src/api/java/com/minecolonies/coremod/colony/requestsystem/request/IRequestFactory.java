package com.minecolonies.coremod.colony.requestsystem.request;

import com.minecolonies.coremod.colony.requestsystem.RequestState;
import com.minecolonies.coremod.colony.requestsystem.factory.IFactory;
import com.minecolonies.coremod.colony.requestsystem.location.ILocatable;
import com.minecolonies.coremod.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface used to specify a factory for requests.
 * Restricts the output type of the general factory interface to IRequest
 * @param <T> The type of request.
 * @param <R> The request type.
 */
public interface IRequestFactory<T, R extends IRequest<T>> extends IFactory<T, R> {

    /**
     * Method to get a new instance of the output given the input and additional context data.
     *
     * @param t       The input to build a new output for.
     * @param context The context of the request.
     * @return The new output instance for a given input.
     * @throws IllegalArgumentException is thrown when the factory cannot produce a new instance out of the given context and input.
     */
    @NotNull
    @Override
    default R getNewInstance(@NotNull T t, @NotNull Object... context) throws IllegalArgumentException {
        if (context.length != 2 && context.length != 3)
            throw new IllegalArgumentException("Unsupported context - Too many parameters.");

        if (!(context[0] instanceof IToken))
            throw new IllegalArgumentException("Unsupported context - First context object is not a token");

        if (!(context[1] instanceof ILocatable))
            throw new IllegalArgumentException("Unsupported context - Second context object should be a location");

        if (context.length == 2)
        {
            return this.getNewInstance(t, context[1], context[0]);
        }

        if (context.length == 3 && !(context[2] instanceof RequestState))
            throw new IllegalArgumentException("Unsupported context - Third context object is not a request state");

        return this.getNewInstance(t, context[1], context[0], context[2]);
    }

    /**
     * Method to get a new instance of a request given the input and token.
     *
     * @param input The input to build a new request for.
     * @param location the location of the requester.
     * @param token The token to build the request from.
     * @return The new output instance for a given input.
     */
    default R getNewInstance(@NotNull T input, @NotNull ILocatable location, @NotNull IToken token)
    {
        return this.getNewInstance(input, location, token, RequestState.CREATED);
    }

    /**
     * Method to get a new instance of a request given the input and token.
     *
     * @param input The input to build a new request for.
     * @param location the location of the requester.
     * @param token The token to build the request from.
     * @param initialState The initial state of the request request.
     * @return The new output instance for a given input.
     */
    R getNewInstance(@NotNull T input, @NotNull ILocatable location, @NotNull IToken token, @NotNull RequestState initialState);
}
