package com.minecolonies.api.colony.requestsystem.factory;

import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import io.netty.buffer.ByteBuf;

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
    default <Input> IFactory<Input, ?> getFactoryForInput(@NotNull String className) throws IllegalArgumentException
    {
        //Simple default implementation grabs the class from a given name and casts it to the proper type.
        //Any exceptions thrown before actual request is made gets wrapped.
        try
        {
            return getFactoryForInput((Class<? extends Input>) Class.forName(className));
        }
        catch (IllegalArgumentException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("The given input name is unknown", ex);
        }
    }

    /**
     * Method to get a factory for a given combination of input and output types.
     *
     * @param inputClass The input type of the factory.
     * @param outputClass The output type of the factory.
     * @param <Input> The input type of the factory
     * @param <Output> The output type of the factory.
     * @return A factory that takes the input type and produces the output type.
     * @throws IllegalArgumentException Thrown when no factory exists for the combination of input and output.
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    <Input, Output> IFactory<Input, Output> getFactoryForIO(@NotNull Class<Input> inputClass, @NotNull Class<Output> outputClass)
      throws IllegalArgumentException;

    /**
     * Method used to get a factory for a given input class.
     *
     * @param clazz   The class of the input type of the requested factory.
     * @param <Input> The type of input for the requested factory.
     * @return The factory that can handle the given input class.
     *
     * @throws IllegalArgumentException is thrown when the given input class is unknown to this Factory Controller.
     */
    <Input> IFactory<Input, ?> getFactoryForInput(@NotNull Class<? extends Input> clazz) throws IllegalArgumentException;

    /**
     * Method used to get a factory for a given Output class name.
     *
     * @param className The class name of the Output type of the requested factory.
     * @param <Output>  The type of Output for the requested factory.
     * @return The factory that can handle the given Output class.
     *
     * @throws IllegalArgumentException is thrown when the given Output name is unknown to this Factory Controller.
     */
    default <Output> IFactory<?, Output> getFactoryForOutput(@NotNull String className) throws IllegalArgumentException
    {
        //Simple default implementation grabs the class from a given name and casts it to the proper type.
        //Any exceptions thrown before actual request is made gets wrapped.
        try
        {
            return getFactoryForOutput((Class<? extends Output>) Class.forName(className));
        }
        catch (IllegalArgumentException ex)
        {
            throw ex;
        }
        catch (Exception ex)
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
    <Output> IFactory<?, Output> getFactoryForOutput(@NotNull Class<? extends Output> clazz) throws IllegalArgumentException;

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
    <Output extends Object> NBTTagCompound serialize(@NotNull Output object) throws IllegalArgumentException;

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
     * Method used to quickly write an object into the given {@link ByteBuf}.
     * @param buffer The buffer to write into.
     * @param object The object to write.
     * @param <Output> The type of the object to write.
     * @throws IllegalArgumentException is thrown when the given output type is unknown to this controller.
     */
    <Output extends Object> void writeToBuffer(@NotNull final ByteBuf buffer, @NotNull final Output object) throws IllegalArgumentException;

    /**
     * Method used to quickly read an object from a given {@link ByteBuf}
     * @param buffer The buffer to read from.
     * @param <Output> The type to read.
     * @return An instance of the given output type, with its stored data from the buffer.
     * @throws IllegalArgumentException is thrown when the requested type is unknown to this controller.
     */
    <Output>Output readFromBuffer(@NotNull final ByteBuf buffer) throws IllegalArgumentException;

    /**
     * Method used to create a new instance of the given input.
     *
     * @param input    The input to process.
     * @param context  The context for the creation.
     * @param <Input>  The type of input.
     * @param <Output> The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     * @throws ClassCastException       thrown when a Factory is known for the given input, but does not produce the given output.
     */
    <Input, Output> Output getNewInstance(@NotNull Input input, @NotNull Object... context) throws IllegalArgumentException, ClassCastException;

    /**
     * Method used to create a new instance of the given output.
     *
     * @param <Output> The type of output.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     * @throws ClassCastException       thrown when a Factory is known for the given input, but does not produce the given output.
     */
    <Output> Output getNewInstance() throws IllegalArgumentException, ClassCastException;
}
