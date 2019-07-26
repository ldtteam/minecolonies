package com.minecolonies.api.colony.requestsystem.resolver;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.RSConstants;
import com.minecolonies.api.util.constant.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Used to resolve a request.
 * In a colony multiple resolvers can exist for a given type R.
 * The resolver with the highest priority is checked first, then second and so forth.
 * <p>
 * The resolver himself is responsible for storing the tokens of requests that he returns
 *
 * @param <R> The request type that this resolver can provide.
 */
public interface IRequestResolver<R extends IRequestable> extends IRequester
{

    /**
     * Used to determine which type of requests can be resolved by this Resolver.
     *
     * @return The class that represents this Type of Request this resolver can resolve.
     */
    @NotNull
    TypeToken<? extends R> getRequestType();

    /**
     * A PreCheck used to determine if this request resolver is able to resolve a given request.
     * Should quickly and cheaply check if this resolver COULD resolve this request.
     *
     * @param requestToCheck The request to check.
     * @param manager        The manager that is checking if this resolver could resolve that request.
     * @return True when this resolver COULD resolve the given request, false when not.
     */
    boolean canResolve(@NotNull IRequestManager manager, IRequest<? extends R> requestToCheck);

    /**
     * Method used to attempt a resolving operation.
     * <p>
     * When this attempt was successful a List with tokens of required requests is returned.
     * This list maybe empty.
     * The list should indicate all sub requests that should be fullfilled before the @code{resolve(IRequest request)} method is called.
     * <p>
     * When this attempt was not successful, eg. this resolver could not schedule a crafting operation, a Null object should be returned.
     * In that case the next resolver will be tried by the manager.
     * <p>
     * IT IS VITAL THAT THE REQUEST RETURNED ARE NOT YET ASSIGNED. SIMULATION AND OTHER STRATEGIES WILL FAIL ELSE!
     * THE MANAGER GIVEN WILL HANDLE ASSIGNING HIMSELF!
     *
     * @param manager The manager that is attempting to resolve using this resolver.
     * @param request The request to resolve.
     * @return The tokens of required requests if the attempt was successful (an empty list is allowed to indicate no requirements), null if the attempt failed.
     */
    @Nullable
    List<IToken<?>> attemptResolve(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

    /**
     * Method used to resolve a given request.
     * Is called the moment all Child requests are resolved.
     * <p>
     * The resolver should update the state through the given manager.
     * <p>
     * When this method is called all requirements need be fullfilled for this resolver.
     * If this is not the case it will throw a RunTimeException
     *
     * @param request The request to resolve.
     * @param manager The manager that is resolving this request, under normal conditions this is the colony manager.
     * @throws RuntimeException is thrown when the resolver could not resolve the request. Should never happen as attemptResolve should be called first,
     *                          and all requirements should be available to this resolver at this point in time.
     */
    void resolve(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

    /**
     * Called by the manager given to indicate that this request has been assigned to you.
     * @param manager The systems manager.
     * @param request The request assigned.
     * @param simulation True when simulating.
     */
    default void onAssignedToThisResolver(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, boolean simulation)
    {
        //Noop
    }

    /**
     * Called by the given manager to indicate that this request is being cancelled or overruled.
     * This is called before the request data is updated, but after the state has been updated.
     *
     * @param manager The manager who is about to update request tree information.
     * @param requestBeingCancelledOrOverruled The request that is being cancelled or overruled.
     */
    default void onRequestAssignedBeingCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> requestBeingCancelledOrOverruled)
    {
        //Noop
    }

    /**
     * Called by the given manager to indicate that this request has been cancelled or overruled.
     * This is called after the request data is updated, and allows for cleanup requests being created, or for updating of external mechanics.
     *
     * @param manager The manager who has updated the request tree information.
     * @param requestCancelledOrOverruled The request that is cancelled or overruled.
     */
    default void onRequestAssignedCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> requestCancelledOrOverruled)
    {
        //Noop
    }

    /**
     * Called by manager given to indicate that a colony has updated their available items.
     * @param manager The systems manager.
     * @param shouldTriggerReassign The request assigned
     */
    default void onColonyUpdate(@NotNull final IRequestManager manager, @NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        //Noop
    }

    /**
     * Method used to indicate that the given request has been overruled by the player.
     * This is called after the children of the given request have been cancelled, but before the request it self is set to a completed state.
     *
     * @param manager The manager that is overruling the request.
     * @param request The request that is being overruled.
     * @throws IllegalArgumentException is thrown when the cancelling failed.
     */
    default void onRequestBeingOverruled(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request)
    {
        //Noop
    }

    /**
     * The priority of this resolver.
     * The higher the priority the earlier this resolver is called.
     *
     * @return The priority of this resolver.
     */
    default int getPriority()
    {
        return RSConstants.CONST_DEFAULT_RESOLVER_PRIORITY;
    }
}
