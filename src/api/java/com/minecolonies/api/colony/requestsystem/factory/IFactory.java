package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe factories.
 *
 * @param <Input>  The input type.
 * @param <Output> The output type.
 */
public interface IFactory<Input, Output>
{
    /**
     * Method to get the type this factory can produce.
     *
     * @return The type of request this factory can produce.
     */
    @NotNull
    TypeToken<? extends Output> getFactoryOutputType();

    /**
     * Used to determine which type this can produce.
     *
     * @return The class that represents the Type of Request this can produce.
     */
    @NotNull
    TypeToken<? extends Input> getFactoryInputType();

    /**
     * Method to get a new instance of the output given the input and additional context data.
     *
     * @param factoryController The controller calling this factory method.
     * @param input             The input to build a new output for.
     * @param context           The context of the request.
     * @return The new output instance for a given input.
     *
     * @throws IllegalArgumentException is thrown when the factory cannot produce a new instance out of the given context and input.
     */
    @NotNull
    Output getNewInstance(@NotNull IFactoryController factoryController, @NotNull Input input, @NotNull Object... context) throws IllegalArgumentException;

    /**
     * Method to serialize a given constructable.
     *
     * @param controller The controller that can be used to serialize complicated types.
     * @param output     The request to serialize.
     * @return The serialized data of the given requets.
     */
    @NotNull
    CompoundNBT serialize(@NotNull IFactoryController controller, @NotNull Output output);

    /**
     * Method to deserialize a given constructable.
     *
     * @param nbt        The data of the request that should be deserialized.
     * @param controller The controller that can be used to deserialize complicated types.
     * @return The request that corresponds with the given data in the nbt
     * @throws Exception if somethings goes wrong during the deserialization.
     */
    @NotNull
    Output deserialize(@NotNull IFactoryController controller, @NotNull CompoundNBT nbt) throws Throwable;
}
