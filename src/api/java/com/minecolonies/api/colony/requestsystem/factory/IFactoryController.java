package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe classes that function as Factory controllers.
 */
public interface IFactoryController
{

    /**
     * Method used to get a factory for a given input class name.
     *
     * @param className The class name of the input type of the requested factory.
     * @param <Input>   The type of input for the requested factory.
     * @return The factory that can handle the given input class.
     *
     * @throws IllegalArgumentException is thrown when the given input name is unknown to this Factory Controller.
     */
    default <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final String className) throws IllegalArgumentException
    {
        //Simple default implementation grabs the class from a given name and casts it to the proper type.
        //Any exceptions thrown before actual request is made gets wrapped.
        try
        {
            return getFactoryForInput((TypeToken<? extends Input>) TypeToken.of(Class.forName(className)));
        }
        catch (final IllegalArgumentException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException("The given input name is unknown", ex);
        }
    }

    /**
     * Method used to get a factory for a given input class.
     *
     * @param clazz   The class of the input type of the requested factory.
     * @param <Input> The type of input for the requested factory.
     * @return The factory that can handle the given input class.
     *
     * @throws IllegalArgumentException is thrown when the given input class is unknown to this Factory Controller.
     */
    <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final TypeToken<? extends Input> clazz) throws IllegalArgumentException;

    /**
     * Method to get a factory for a given combination of input and output types.
     *
     * @param inputTypeToken  The input type of the factory.
     * @param outputTypeToken The output type of the factory.
     * @param <Input>         The input type of the factory
     * @param <Output>        The output type of the factory.
     * @return A factory that takes the input type and produces the output type.
     *
     * @throws IllegalArgumentException Thrown when no factory exists for the combination of input and output.
     */
    <Input, Output> IFactory<Input, Output> getFactoryForIO(@NotNull final TypeToken<? extends Input> inputTypeToken, @NotNull final TypeToken<? extends Output> outputTypeToken)
      throws IllegalArgumentException;

    /**
     * Method used to get a factory for a given Output class name.
     *
     * @param className The class name of the Output type of the requested factory.
     * @param <Output>  The type of Output for the requested factory.
     * @return The factory that can handle the given Output class.
     *
     * @throws IllegalArgumentException is thrown when the given Output name is unknown to this Factory Controller.
     */
    default <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final String className) throws IllegalArgumentException
    {
        //Simple default implementation grabs the class from a given name and casts it to the proper type.
        //Any exceptions thrown before actual request is made gets wrapped.
        try
        {
            return getFactoryForOutput((TypeToken<? extends Output>) TypeToken.of(Class.forName(className)));
        }
        catch (final IllegalArgumentException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException("The given output name is unknown", ex);
        }
    }

    /**
     * Method used to get a factory for a given output class.
     *
     * @param clazz    The class of the output type of the requested factory.
     * @param <Output> The type of output for the requested factory.
     * @return The factory that can handle the given output class.
     *
     * @throws IllegalArgumentException is thrown when the given output class is unknown to this Factory Controller.
     */
    <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final TypeToken<? extends Output> clazz) throws IllegalArgumentException;

    /**
     * Method used to register a new factory to this controller.
     *
     * @param factory  The new factory.
     * @param <Input>  The type of input the factory accepts.
     * @param <Output> The type of output the factory produces.
     * @throws IllegalArgumentException if there is already a factory registered with either the given input and/or the given output.
     */
    <Input, Output> void registerNewFactory(@NotNull final IFactory<Input, Output> factory) throws IllegalArgumentException;

    /**
     * Method used to quickly serialize a object if it is known to this controller.
     *
     * @param object The object to serialize.
     * @param <Output> the output type.
     * @return An NBTTag containing a serialized version of the given object.
     *
     * @throws IllegalArgumentException is thrown when the output type is unknown to this controller.
     */
    <Output extends Object> CompoundNBT serialize(@NotNull final Output object) throws IllegalArgumentException;

    /**
     * Method used to quickly deserialize a object if it is known to this controller.
     *
     * @param compound The data to deserialize an object from.
     * @param <Output> The type of object to deserialize.
     * @return The deserialized version of the given data.
     *
     * @throws IllegalArgumentException is thrown when the type stored in the data is unknown to this controller.
     */
    <Output> Output deserialize(@NotNull final CompoundNBT compound) throws IllegalArgumentException;

    /**
     * Method used to quickly write an object into the given {@link ByteBuf}.
     *
     * @param buffer   The buffer to write into.
     * @param object   The object to write.
     * @param <Output> The type of the object to write.
     * @throws IllegalArgumentException is thrown when the given output type is unknown to this controller.
     */
    <Output extends Object> void writeToBuffer(@NotNull final PacketBuffer buffer, @NotNull final Output object) throws IllegalArgumentException;

    /**
     * Method used to quickly read an object from a given {@link ByteBuf}
     *
     * @param buffer   The buffer to read from.
     * @param <Output> The type to read.
     * @return An instance of the given output type, with its stored data from the buffer.
     *
     * @throws IllegalArgumentException is thrown when the requested type is unknown to this controller.
     */
    <Output> Output readFromBuffer(@NotNull final PacketBuffer buffer) throws IllegalArgumentException;

    /**
     * Method used to create a new instance of the given input.
     *
     * @param requestedType The typetoken for the requested type.
     * @param input         The input to process.
     * @param context       The context for the creation.
     * @param <Input>       The type of input.
     * @param <Output>      The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     * @throws ClassCastException       thrown when a Factory is known for the given input, but does not produce the given output.
     */
    <Input, Output> Output getNewInstance(@NotNull final TypeToken<? extends Output> requestedType, @NotNull final Input input, @NotNull final Object... context)
      throws IllegalArgumentException, ClassCastException;

    /**
     * Method used to create a new instance of the given output.
     *
     * @param requestedType The typetoken of the requested type.
     * @param <Output>      The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     * @throws ClassCastException       thrown when a Factory is known for the given input, but does not produce the given output.
     */
    <Output> Output getNewInstance(@NotNull final TypeToken<? extends Output> requestedType) throws IllegalArgumentException, ClassCastException;

    /**
     * Method used to register a TypeOverride handler.
     * Useful if the Client side has a different Handler.
     * Or if an upgrade between version is needed.
     *
     * @param overrideHandler The override handler to register.
     * @param <Output>        The output type that the handler converts to.
     */
    <Output> void registerNewTypeOverrideHandler(@NotNull final ITypeOverrideHandler<Output> overrideHandler);

    /**
     * Method used to register a renaming of a class.
     * @param previousName The old class name.
     * @param newName The new class name.
     */
    void registerNewClassRenaming(@NotNull final String previousName, @NotNull final String newName);
}
