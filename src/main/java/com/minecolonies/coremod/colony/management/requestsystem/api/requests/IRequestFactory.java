package com.minecolonies.coremod.colony.management.requestsystem.api.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Marc on 23-2-2017.
 */
public interface IRequestFactory<T, R extends IRequest<T>, D extends NBTBase> {

    /**
     * Method to get the request type this factory can produce.
     * @return The type of request this factory can produce.
     */
    @NotNull
    Class<R> getFactoryOutputType();

    /**
     * Used to determine which type of request this can produce.
     * @return The class that represents the Type of Request this can produce.
     */
    @NotNull
    Class<? extends T> getRequestType();

    /**
     * Method to get a new instance of a request produced by this factory.
     * @param requestedObject The object to build a request for.
     * @param token The token that represents the constructed request to the outside.
     * @return The request for the given object.
     */
    @NotNull
    R getNewInstance(@NotNull T requestedObject, IRequestToken token);

    /**
     * Method to serialize a given Request.
     * @param request The request to serialize.
     * @return The serialized data of the given requets.
     */
    @NotNull
    D serializeRequest(@NotNull R request);

    /**
     * Method to deserialize a given Request.
     * @param nbt The data of the request that should be deserialized.
     * @return The request that corresponds with the given data in the nbt
     */
    @NotNull
    R deserializeRequest(@NotNull D nbt);
}
