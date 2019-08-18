package com.minecolonies.api.colony.requestsystem.resolver;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
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
    TypeToken<? extends R> getRequestType();

    /**
     * A PreCheck used to determine if this request resolver is able to resolve a given request.
     * Should quickly and cheaply check if this resolver COULD resolve this request.
     *
     * @param requestToCheck The request to check.
     * @param manager        The manager that is checking if this resolver could resolve that request.
     * @return True when this resolver COULD resolve the given request, false when not.
     */
    boolean canResolveRequest(@NotNull IRequestManager manager, IRequest<? extends R> requestToCheck);

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
    List<IToken<?>> attemptResolveRequest(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

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
    @Nullable
    void resolveRequest(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

    /**
     * Called by the manager given to indicate that this request has been assigned to you.
     * @param manager The systems manager.
     * @param request The request assigned.
     * @param simulation True when simulating.
     */
    default void onRequestAssigned(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends R> request, boolean simulation)
    {
        //Noop
    }

    /**
     * Indicates that a assigned request has been cancelled.
     * Is called before graph is updated.
     *
     * @param manager The manager that indicates the cancelling
     * @param request The request that has been cancelled.
     */
    void onAssignedRequestBeingCancelled(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

    /**
     * Indicates that a assigned request has been cancelled.
     * Is called after the graph has been updated.
     *
     * @param manager The manager that indicates the cancelling
     * @param request The request that has been cancelled.
     */
    void onAssignedRequestCancelled(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> request);

    /**
     * Called by manager given to indicate that a colony has updated their available items.
     * @param manager The systems manager.
     * @param shouldTriggerReassign The request assigned
     */
    default void onColonyUpdate(@NotNull final IRequestManager manager, @NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        //Noop
    }

    @Nullable
    default List<IRequest<?>> getFollowupRequestForCompletion(@NotNull IRequestManager manager, @NotNull IRequest<? extends R> completedRequest)
    {
        return Lists.newArrayList();
    }

    /**
     * The priority of this resolver.
     * The higher the priority the earlier this resolver is called.
     *
     * @return The priority of this resolver.
     */
    int getPriority();
}