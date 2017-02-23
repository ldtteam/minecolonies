package com.minecolonies.coremod.colony.management.requestsystem.api;

import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequestFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to manage Requests in a Colony
 */
public interface IRequestManager extends INBTSerializable<NBTTagCompound> {

    /**
     * Method to retrieve an IRequestFactory for a given request implementation type.
     *
     * This method is internally used to retrieve data while deserializing.
     * @param type The type of request you are requesting a factory for.
     * @param <T> The type that the request provides.
     * @param <R> The type of request implementation that a factory is requested for.
     * @return The factory that can produce a given request of type T.
     * @throws IllegalArgumentException is thrown when there is no factory known to this requestmanager that can produce a request of the given type.
     */
    <T, R extends IRequest<T>> IRequestFactory<T,R,?> getFactoryForRequestType(Class<R> type) throws IllegalArgumentException;

    /**
     * Method to retrieve an IRequestFactory for a given requested Object.
     * @param object The object you are requesting and want a requestfactory for.
     * @param <T> The type of object you want to request.
     * @return The factory that can create a Request for a given object.
     * @throws IllegalArgumentException is thrown when this Requestmanager has no factory for the given object.
     */
    <T> IRequestFactory<T, ? extends IRequest<T>, ?> getFactoryForRequest(T object) throws IllegalArgumentException;

    /**
     * Method to create a request for a given object
     * @param object The Object that is being requested.
     * @param <T> The type of request.
     * @return The token representing the request.
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    <T> IRequestToken createRequest(@NotNull T object) throws IllegalArgumentException;

    /**
     * Method to get a request for a given token.
     * @param token The token of a given request.
     * @param <T> The type of request requested.
     * @return The request corresponding to this token.
     * @throws IllegalArgumentException is thrown when the token is unknown to the manager.
     */
    @NotNull
    <T> IRequest<T> getRequestForToken(@NotNull IRequestToken token) throws IllegalArgumentException;
}
