package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.AssigningStrategy;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.Collection;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

public interface IRequestHandler
{
    IRequestManager getManager();

    @SuppressWarnings(UNCHECKED)
    <Request extends IRequestable> IRequest<Request> createRequest(IRequester requester, Request request);

    void registerRequest(IRequest<?> request);

    /**
     * Method used to assign a given request to a resolver. Does not take any blacklist into account.
     *
     * @param request The request to assign
     * @throws IllegalArgumentException when the request is already assigned
     */
    @SuppressWarnings(UNCHECKED)
    void assignRequest(IRequest<?> request);

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
     *
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @SuppressWarnings(UNCHECKED)
    IToken<?> assignRequest(IRequest<?> request, Collection<IToken<?>> resolverTokenBlackList);

    /**
     * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account. Uses the default assigning strategy: {@link
     * AssigningStrategy#PRIORITY_BASED}
     *
     * @param request                The request to assign.
     * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
     */
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    IToken<?> assignRequestDefault(IRequest request, Collection<IToken<?>> resolverTokenBlackList);

    /**
     * Method used to reassign the request to a resolver that is not in the given blacklist. Cancels the request internally without notify the requester, and attempts a reassign.
     * If the reassignment failed, it is assigned back to the orignal resolver.
     *
     * @param request                The request that is being reassigned.
     * @param resolverTokenBlackList The blacklist to which not to assign the request.
     * @return The token of the resolver that has gotten the request assigned, null if none was found.
     * @throws IllegalArgumentException Thrown when something went wrong.
     */
    IToken<?> reassignRequest(IRequest<?> request, Collection<IToken<?>> resolverTokenBlackList);

    /**
     * Method used to check if a given request token is assigned to a resolver.
     *
     * @param token The request token to check for.
     * @return True when the request token has been assigned, false when not.
     */
    boolean isAssigned(IToken<?> token);

    /**
     * Method used to handle the successful resolving of a request.
     *
     * @param token The token of the request that got finished successfully.
     */
    @SuppressWarnings(UNCHECKED)
    void onRequestSuccessful(IToken<?> token);

    /**
     * Method used to handle requests that were overruled or cancelled. Cancels all children first, handles the creation of clean up requests.
     *
     * @param token The token of the request that got cancelled or overruled
     */
    @SuppressWarnings(UNCHECKED)
    void onRequestOverruled(IToken<?> token);

    /**
     * Method used to handle requests that were overruled or cancelled. Cancels all children first, handles the creation of clean up requests.
     *
     * @param token The token of the request that got cancelled or overruled
     */
    @SuppressWarnings(UNCHECKED)
    void onRequestCancelled(IToken<?> token);

    /**
     * Method used to handle cancellation internally without notifying the requester that the request has been cancelled.
     *
     * @param token The token which is internally processed.
     */
    @SuppressWarnings(UNCHECKED)
    void processInternalCancellation(IToken<?> token);

    /**
     * Method used during clean up to process Parent replacement.
     *
     * @param target    The target request, which gets their parent replaced.
     * @param newParent The new cleanup request used to cleanup the target when it is finished.
     */
    @SuppressWarnings({RAWTYPES, UNCHECKED})
    void processParentReplacement(IRequest target, IRequest newParent);

    /**
     * Method used to resolve a request. When this method is called the given request has to be assigned.
     *
     * @param request The request about to be resolved.
     * @throws IllegalArgumentException when the request is unknown, not resolved, or cannot be resolved.
     */
    @SuppressWarnings({UNCHECKED, RAWTYPES})
    void resolveRequest(IRequest request);

    /**
     * Method called when the given manager gets notified of the receiving of a given task by its requester. All communication with the resolver should be aborted by this time, so
     * overrullings and cancelations need to be processed, before this method is called.
     *
     * @param token The token of the request.
     * @throws IllegalArgumentException Thrown when the token is unknown.
     */
    void cleanRequestData(IToken<?> token);

    /**
     * Method used to get a registered request from a given token.
     *
     * @param token The token to query
     * @throws IllegalArgumentException when the token is unknown to the given manager.
     */
    @SuppressWarnings(RAWTYPES)
    IRequest getRequest(IToken<?> token);

    /**
     * Method used to get a registered request fora given token.
     *
     * @param token The token to get the request for.
     * @return The request or null when no request with that token exists.
     */
    @SuppressWarnings(RAWTYPES)
    IRequest getRequestOrNull(IToken<?> token);

    /**
     * Returns all requests made by a given requester.
     *
     * @param requester The requester in question.
     * @return A collection with request instances that are made by the given requester.
     */
    Collection<IRequest<?>> getRequestsMadeByRequester(IRequester requester);
}
