package com.minecolonies.api.colony.requestsystem;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of a FactoryController
 * Singleton.
 */
public final class StandardFactoryController implements IFactoryController
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TYPE = "Type";
    private static final String NBT_DATA = "Data";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Instance variable.
     */
    private static final StandardFactoryController INSTANCE       = new StandardFactoryController();
    /**
     * Input mappings.
     */
    @NotNull
    private final        Map<Class, IFactory>      inputMappings  = new HashMap<>();
    /**
     * Output mappings.
     */
    @NotNull
    private final        Map<Class, IFactory>      outputMappings = new HashMap<>();

    /**
     * Private constructor. Throws IllegalStateException if already created.
     */
    private StandardFactoryController()
    {
        if (INSTANCE != null)
        {
            throw new IllegalStateException("StandardFactoryController");
        }
    }

    /**
     * Method to get the current instance of the StandardFactoryController
     *
     * @return The current instance of the standard factory controller.
     */
    public static StandardFactoryController getInstance()
    {
        return INSTANCE;
    }

    /**
     * Method used to get a factory for a given input class.
     *
     * @param clazz The class of the input type of the requested factory.
     * @return The factory that can handle the given input class.
     *
     * @throws IllegalArgumentException is thrown when the given input class is unknown to this Factory Controller.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final Class<? extends Input> clazz) throws IllegalArgumentException
    {
        if (!inputMappings.containsKey(clazz))
        {
            throw new IllegalArgumentException("The given class(name) has no been registered to this Factory.");
        }

        return inputMappings.get(clazz);
    }

    /**
     * Method used to get a factory for a given output class.
     *
     * @param clazz The class of the output type of the requested factory.
     * @return The factory that can handle the given output class.
     *
     * @throws IllegalArgumentException is thrown when the given output class is unknown to this Factory Controller.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final Class<? extends Output> clazz) throws IllegalArgumentException
    {
        if (!inputMappings.containsKey(clazz))
        {
            throw new IllegalArgumentException("The given class(name) has not been registered to this Factory.");
        }

        return outputMappings.get(clazz);
    }

    /**
     * Method used to register a new factory to this controller.
     *
     * @param factory  The new factory.
     * @param <Input>  The type of input the factory accepts.
     * @param <Output> The type of output the factory produces.
     * @throws IllegalArgumentException if there is already a factory registered with either the given input and/or the given output.
     */
    @Override
    public <Input, Output> void registerNewFactory(@NotNull final IFactory<Input, Output> factory) throws IllegalArgumentException
    {
        if (inputMappings.containsKey(factory.getFactoryInputType()))
        {
            throw new IllegalArgumentException("A factory with the given input type is already registered!");
        }

        if (outputMappings.containsKey(factory.getFactoryOutputType()))
        {
            throw new IllegalArgumentException("A factory with the given output type is already registered!");
        }

        Log.getLogger()
          .debug("Registering factory: " + factory.toString() + " with input: " + factory.getFactoryInputType().getName() + " and output: " + factory.getFactoryOutputType() + ".");
        inputMappings.put(factory.getFactoryInputType(), factory);
        outputMappings.put(factory.getFactoryOutputType(), factory);
    }

    /**
     * Method used to quickly serialize a object if it is known to this controller.
     *
     * @param object The object to serialize.
     * @return An NBTTag containing a serialized version of the given object.
     *
     * @throws IllegalArgumentException is thrown when the output type is unknown to this controller.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Output> NBTTagCompound serialize(@NotNull final Output object) throws IllegalArgumentException
    {
        final NBTTagCompound compound = new NBTTagCompound();

        final IFactory<?, Output> factory = getFactoryForOutput((Class<? extends Output>) object.getClass());

        compound.setString(NBT_TYPE, object.getClass().getName());
        compound.setTag(NBT_DATA, factory.serialize(this, object));

        return compound;
    }

    /**
     * Method used to quickly deserialize a object if it is known to this controller.
     *
     * @param compound The data to deserialize an object from.
     * @return The deserialized version of the given data.
     *
     * @throws IllegalArgumentException is thrown when the type stored in the data is unknown to this controller.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Output> Output deserialize(@NotNull final NBTTagCompound compound) throws IllegalArgumentException
    {
        final String className = compound.getString(NBT_TYPE);

        final IFactory<?, Output> factory = getFactoryForOutput(className);
        return factory.deserialize(this, compound.getCompoundTag(NBT_DATA));
    }

    /**
     * Method used to create a new instance of the given input.
     *
     * @param input   The input to process.
     * @param context The context for the creation.
     * @return The output from the factory, created by the given input and output.
     *
     * @throws IllegalArgumentException thrown when the output and input do not match a factory known to this controller.
     * @throws ClassCastException       thrown when a Factory is known for the given input, but does not produce the given output.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Input, Output> Output getNewInstance(@NotNull final Input input, @NotNull final Object... context) throws IllegalArgumentException, ClassCastException
    {
        final IFactory<Input, Output> factory = (IFactory<Input, Output>) getFactoryForInput((Class<? extends Input>) input.getClass());

        return factory.getNewInstance(input, context);
    }
}
