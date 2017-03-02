package com.minecolonies.coremod.colony.management.requestsystem.api.request;

import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.api.factory.IFactory;
import com.minecolonies.coremod.colony.management.requestsystem.api.token.IToken;
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
        if (context.length != 1  && context.length != 2)
            throw new IllegalArgumentException("Unsupported context - Too many parameters. Only token or token state combination is needed.!");

        if (!(context[0] instanceof IToken))
            throw new IllegalArgumentException("Unsupported context - First context object is not a token");

        if (context.length == 2 && !(context[1] instanceof RequestState))
            throw new IllegalArgumentException("Unsupported context - Second context object is not a request state");

        return this.getNewInstance(t, (IToken) context[0]);
    }

    /**
     * Method to get a new instance of a request given the input and token.
     *
     * @param input The input to build a new request for.
     * @param token The token to build the request from.
     * @return The new output instance for a given input.
     */
    default R getNewInstance(@NotNull T input, @NotNull IToken token) {
        return this.getNewInstance(input, token, RequestState.CREATED);
    }

    /**
     * Method to get a new instance of a request given the input and token.
     *
     * @param input The input to build a new request for.
     * @param token The token to build the request from.
     * @param initialState The initial state of the request request.
     * @return The new output instance for a given input.
     */
    R getNewInstance(@NotNull T input, @NotNull IToken token, @NotNull RequestState initialState);


}
