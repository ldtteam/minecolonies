package com.minecolonies.coremod.colony.management.requestsystem.api;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.factory.IFactoryController;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocatable;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.token.IRequestToken;
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
     * Method used to get the FactoryController of the RequestManager.
     * @return The FactoryController of this RequestManager.
     */
    @NotNull
    IFactoryController getFactoryController();

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
