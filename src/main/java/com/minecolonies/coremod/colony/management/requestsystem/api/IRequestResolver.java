package com.minecolonies.coremod.colony.management.requestsystem.api;

import org.jetbrains.annotations.Nullable;

/**
 * Used to resolve a request.
 * In a colony multiple resolvers can exist for a given type T.
 * The resolver with the highest priority is checked first, then second and so forth.
 * @param <T>
 */
public interface IRequestResolver<T> {

    /**
     * Used to determine which type of requests can be resolved by this Resolver.
     *
     * @return The class that represents this Type of Request this resolver can resolve.
     */
    Class<? extends T> getRequestType();

    /**
     * A PreCheck used to determine if this request resolver is able to resolve a given request.
     * Should quickly and cheaply check if this resolver COULD resolve this request.
     *
     * @param requestToCheck The request to check.
     * @return True when this resolver COULD resolve the given request, false when not.
     */
    boolean canResolve(IRequest<T> requestToCheck);

    /**
     * Method used to attempt a resolving operation for a given request.
     * A successful resolve returns a NonNull Object.
     * A non successful resolve returns Null
     *
     * @param request The request to resolve.
     * @return The result of the resolving operation. Null if not successful.
     */
    @Nullable
    IRequestResult<T> resolve(IRequest<T> request);

    /**
     * The priority of this resolver.
     * The higher the priority the earlier this resolver is called.
     *
     * @return The priority of this resolver.
     */
    int getPriority();
}
