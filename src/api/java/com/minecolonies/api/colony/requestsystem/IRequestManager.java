package com.minecolonies.api.colony.requestsystem;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe classes that function as managers for requests inside a colony.
 * Extends INBTSerializable to allow for easy reading and writing from NBT.
 */
public interface IRequestManager extends INBTSerializable<NBTTagCompound>
{

    /**
     * The colony this manager manages the requests for.
     *
     * @return The colony this manager manages the requests for.
     */
    @NotNull
    IColony getColony();

    /**
     * Method used to get the FactoryController of the RequestManager.
     *
     * @return The FactoryController of this RequestManager.
     */
    @NotNull
    IFactoryController getFactoryController();

    /**
     * Method to create a request for a given object
     *
     * @param requester The requester.
     * @param object    The Object that is being requested.
     * @param <T>       The type of request.
     * @return The token representing the request.
     *
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    <T> IToken createRequest(@NotNull IRequester requester, @NotNull T object) throws IllegalArgumentException;

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @NotNull
    void assignRequest(@NotNull IToken token) throws IllegalArgumentException;

    /**
     * Method used to create and immediately assign a request.
     *
     * @param requester The requester of the requestable.
     * @param object    The requestable
     * @param <T>       The type of the requestable
     * @return The token that represents the request.
     *
     * @throws IllegalArgumentException when either createRequest or assignRequest have thrown an IllegalArgumentException
     */
    @NotNull
    <T> IToken createAndAssignRequest(@NotNull IRequester requester, @NotNull T object) throws IllegalArgumentException;

    /**
     * Method to get a request for a given token.
     *
     * @param token The token to get a request for.
     * @param <T>   The type of request that is being looked for.
     * @return The request of the given type for that token.
     *
     * @throws IllegalArgumentException when either their is no request with that token, or the token does not produce a request of the given type T.
     */
    @NotNull
    <T> IRequest<T> getRequestForToken(@NotNull IToken token) throws IllegalArgumentException;

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @NotNull
    void updateRequestState(@NotNull IToken token, @NotNull RequestState state) throws IllegalArgumentException;

    /**
     * Method used to indicate to this manager that a new Provider has been added to the colony.
     *
     * @param provider The new provider.
     * @throws IllegalArgumentException is thrown when a provider with the same token is already registered.
     */
    void onProviderAddedToColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException;

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     * @throws IllegalArgumentException is thrown when no provider with the same token is registered.
     */
    void onProviderRemovedFromColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException;
}
