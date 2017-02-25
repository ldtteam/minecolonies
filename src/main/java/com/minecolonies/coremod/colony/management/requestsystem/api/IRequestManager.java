package com.minecolonies.coremod.colony.management.requestsystem.api;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocatable;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocation;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocationFactory;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequestFactory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe classes that function as managers for requests inside a colony.
 * Extends INBTSerializable to allow for easy reading and writing from NBT.
 */
public interface IRequestManager extends INBTSerializable<NBTTagCompound> {

    /**
     * The colony this manager manages the requests for.
     * @return The colony this manager manages the requests for.
     */
    @NotNull
    IColony getColony();

    /**
     * Method to retrieve an IRequestFactory for a given request implementation type.
     *
     * This method is internally used to retrieve data while deserializing.
     * @param type The type name of request you are requesting a factory for.
     * @param <T> The type that the request provides.
     * @param <R> The type of request implementation that a factory is requested for.
     * @return The factory that can produce a given request of type T.
     * @throws IllegalArgumentException is thrown when there is no factory known to this requestmanager that can produce a request of the given type.
     */
    @NotNull
    <T, R extends IRequest<T>> IRequestFactory<T,R,?> getFactoryForRequestType(String type) throws IllegalArgumentException;

    /**
     * Method to retrieve an IRequestFactory for a given requested Object.
     * @param object The object you are requesting and want a requestfactory for.
     * @param <T> The type of object you want to request.
     * @return The factory that can create a Request for a given object.
     * @throws IllegalArgumentException is thrown when this Requestmanager has no factory for the given object.
     */
    @NotNull
    <T> IRequestFactory<T, ? extends IRequest<T>, ?> getFactoryForRequest(T object) throws IllegalArgumentException;

    /**
     * Method to retrieve an ILocationFactory for a given location implementation type.
     * @param type The type name of the location you are requestinga factory for.
     * @param <T> The actual location type.
     * @return The factory for the type you requested.
     * @throws IllegalArgumentException is thrown when the type is unknown to this manager.
     */
    @NotNull
    <T extends ILocation> ILocationFactory<T, ? extends NBTBase> getFactoryForLocation(String type) throws IllegalArgumentException;

    /**
     * Method to retrieve an ILocationFactory for a given location implementation.
     * @param location The location to get the factory for.
     * @param <T> The type of location you want the factory for.
     * @return The factory for a given location.
     * @throws IllegalArgumentException is thrown when the type is unknown to this manager.
     */
    @NotNull
    <T extends ILocation> ILocationFactory<T, ? extends NBTBase> getFactoryForLocation(T location) throws IllegalArgumentException;

    /**
     * Method to create a request for a given object
     * @param requester The requester.
     * @param object The Object that is being requested.
     * @param <T> The type of request.
     * @return The token representing the request.
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    <T> IRequestToken createRequest(@NotNull ILocatable requester, @NotNull T object) throws IllegalArgumentException;

    /**
     * Method to get a request for a given token.
     * @param token The token to get a request for.
     * @param <T> The type of request that is being looked for.
     * @return The request of the given type for that token.
     * @throws IllegalArgumentException when either their is no request with that token, or the token does not produce a request of the given type T.
     */
    @NotNull
    <T> IRequest<T> getRequestForToken(@NotNull IRequestToken token) throws IllegalArgumentException;

    /**
     * Method to update the state of a given request.
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @NotNull
    void updateRequestState(@NotNull IRequestToken token, @NotNull RequestState state) throws IllegalArgumentException;


}
