package com.minecolonies.coremod.colony.management.requestsystem.api;

import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Used to resolve a request.
 * In a colony multiple resolvers can exist for a given type R.
 * The resolver with the highest priority is checked first, then second and so forth.
 * @param <R> The request type that this resolver can provide.
 */
public interface IRequestResolver<R> {

    /**
     * Used to determine which type of requests can be resolved by this Resolver.
     *
     * @return The class that represents this Type of Request this resolver can resolve.
     */
    Class<? extends R> getRequestType();

    /**
     * A PreCheck used to determine if this request resolver is able to resolve a given request.
     * Should quickly and cheaply check if this resolver COULD resolve this request.
     *
     * @param requestToCheck The request to check.
     * @param manager The manager that is checking if this resolver could resolve that request.
     * @return True when this resolver COULD resolve the given request, false when not.
     */
    boolean canResolve(@NotNull IRequestManager manager, IRequest<R> requestToCheck);

    /**
     * Method used to attempt a resolving operation.
     *
     * <p>
     *     When this attempt was successful a List with requirement requests is returned.
     *     This list maybe empty.
     *     The list should indicate all sub requests that should be fullfilled before the @code{resolve(IRequest request)} method is called.
     * </p>
     *
     * <p>
     *     When this attempt was not successful, eg. this resolver could not schedule a crafting operation, a Null object should be returned.
     *     In that case the next resolver will be tried by the manager.
     * </p>
     * @param request The request to resolve.
     * @param manager The manager that is attempting to resolve using this resolver.
     * @return The requirements of the request if the attempt was successful (an empty list is allowed to indicate no requirements), null if the attempt failed.
     */
    @Nullable
    List<IRequest> attemptResolve(@NotNull IRequestManager manager, @NotNull IRequest<R> request);

    /**
     * Method used to resolve given request.
     * <p>
     * When this method is called all requirements should be fullfilled for this resolver.
     * If this is not the case it will throw a RunTimeException
     * </p>
     *
     * A successful resolve returns a NonNull Object.
     *
     * @param request The request to resolve.
     * @param manager The manager that is resolving this request.
     * @return The result of the resolving operation.
     * @throws RuntimeException is thrown when the resolver could not resolve the request. Should never happen as attemptResolve should be called first,
     *                          and all requirements should be available to this resolver at this point in time.
     */
    @NotNull
    R resolve(@NotNull IRequestManager manager, IRequest<R> request) throws RuntimeException;

    /**
     * The priority of this resolver.
     * The higher the priority the earlier this resolver is called.
     *
     * @return The priority of this resolver.
     */
    int getPriority();
}
