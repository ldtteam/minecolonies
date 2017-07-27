package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe classes that function as Factory controllers.
 */
public interface IFactoryController
{
    /**
     * Method used to get a factory for a given input {@link TypeToken}.
     *
     * @param inputTypeToken The typetoken for the input type to process.
     * @param <Input>        The type of input for the requested factory.
     * @return The factory that can handle the given input class.
     *
     * @throws IllegalArgumentException is thrown when the given input class is unknown to this Factory Controller.
     */
    <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final TypeToken<Input> inputTypeToken) throws IllegalArgumentException;

    /**
     * Method used to get a factory for a given output {@link TypeToken}.
     *
     * @param outputTypeToken The typetoken for the output type to process.
     * @param <Output>        The type of output for the requested factory.
     * @return The factory that can handle the given output class.
     *
     * @throws IllegalArgumentException is thrown when the given output class is unknown to this Factory Controller.
     */
    <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException;

    /**
     * Method used to get a factory for a given input AND output.
     *
     * @param inputTypeToken  The type token for the input type to process.
     * @param outputTypeToken The type token of the output type to process.
     * @param <Input>         The type of input that the factory takes.
     * @param <Output>        The type of output that the factory produces.
     * @return A IFactory that takes the input types as input, and produces the output type as output.
     *
     * @throws IllegalArgumentException is thrown when no factory is registered with the given input and output.
     */
    <Input, Output> IFactory<Input, Output> getFactoryForIO(@NotNull final TypeToken<Input> inputTypeToken, @NotNull final TypeToken<Output> outputTypeToken)
      throws IllegalArgumentException;

    /**
     * Method used to register a new factory to this controller.
     *
     * @param factory  The new factory.
     * @param <Input>  The type of input the factory accepts.
     * @param <Output> The type of output the factory produces.
     * @throws IllegalArgumentException if there is already a factory registered with either the given input and/or the given output.
     */
    <Input, Output> void registerNewFactory(@NotNull IFactory<Input, Output> factory) throws IllegalArgumentException;

    /**
     * Method used to quickly serialize a object if it is known to this controller.
     *
     * @param object The object to serialize.
     * @return An NBTTag containing a serialized version of the given object.
     *
     * @throws IllegalArgumentException is thrown when the output type is unknown to this controller.
     */
    <Output> NBTTagCompound serialize(@NotNull Output object) throws IllegalArgumentException;

    /**
     * Method used to quickly deserialize a object if it is known to this controller.
     *
     * @param compound The data to deserialize an object from.
     * @param <Output> The type of object to deserialize.
     * @return The deserialized version of the given data.
     *
     * @throws IllegalArgumentException is thrown when the type stored in the data is unknown to this controller.
     */
    <Output> Output deserialize(@NotNull NBTTagCompound compound) throws IllegalArgumentException;

    /**
     * Method used to create a new instance of the given input.
     *
     * @param input           The input to process.
     * @param outputTypeToken The typetoken for the output type.
     * @param context         The context for the creation.
     * @param <Input>         The type of input.
     * @param <Output>        The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     */
    <Input, Output> Output getNewInstance(@NotNull Input input, @NotNull final TypeToken<Output> outputTypeToken, @NotNull Object... context) throws IllegalArgumentException;

    /**
     * Method used to create a new instance with no parameters.
     *
     * @param outputTypeToken The typetoken of the output type.
     * @param <Output>        The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and the {@link FactoryVoidInput} do not match a factory known to this controller.
     */
    <Output> Output getNewInstance(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException;
}
