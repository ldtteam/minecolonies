package com.minecolonies.api.colony.requestsystem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.Suppression;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of a FactoryController
 * Singleton.
 */
public final class StandardFactoryController implements IFactoryController
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    public static final String NBT_TYPE = "Type";
    public static final String NBT_DATA = "Data";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Instance variable.
     */
    private static final StandardFactoryController INSTANCE              = new StandardFactoryController();
    /**
     * Primary (main) Input mappings.
     */
    @NotNull
    private final        Map<Class, IFactory>  primaryInputMappings  = new HashMap<>();
    /**
     * Primary (main) Output mappings.
     */
    @NotNull
    private final        Map<Class, IFactory>  primaryOutputMappings = new HashMap<>();

    /**
     * Secondary (super) output mappings
     */
    @NotNull
    private final Map<Class, Set<IFactory>>            secondaryOutputMappings = new HashMap<>();
    /**
     * A cache that holds all Mappers and their search secondary IO types.
     * Filled during runtime to speed up searches to factories when both Input and Output type are secondary types.
     */
    @NotNull
    private final Cache<Tuple<Class, Class>, IFactory> secondaryMappingsCache  = CacheBuilder.newBuilder().build();

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
     * Resets the FactoryController to the default values.
     * Clears all registered Factories.
     * <p>
     * Only used for testing.
     */
    public static void reset()
    {
        getInstance().primaryInputMappings.clear();
        getInstance().primaryOutputMappings.clear();
        getInstance().secondaryOutputMappings.clear();
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

    @SuppressWarnings(Suppression.UNCHECKED)
    @Override
    public <Input, Output> IFactory<Input, Output> getFactoryForIO(@NotNull final Class<Input> inputClass, @NotNull final Class<Output> outputClass)
      throws IllegalArgumentException
    {
        try
        {
            //Request from cache or search.
            return secondaryMappingsCache.get(new Tuple<>(inputClass, outputClass), () ->
            {
                Log.getLogger().debug("Attempting to find a Factory with Primary: " + inputClass.toString() + " -> " + outputClass.toString());

                final Set<Class> secondaryInputSet = ReflectionUtils.getSuperClasses(inputClass);

                for (final Class secondaryInputClass : secondaryInputSet)
                {
                    final IFactory factory = primaryInputMappings.get(secondaryInputClass);
                    if (factory == null)
                    {
                        continue;
                    }

                    Log.getLogger().debug("Found matching Factory for Primary input type.");
                    final Set<TypeToken> secondaryOutputSet = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());
                    if (secondaryOutputSet.contains(outputClass))
                    {
                        Log.getLogger().debug("Found input factory with matching super Output type. Search complete with: " + factory);
                        return factory;
                    }
                }

                throw new IllegalArgumentException("No factory found with the given IO types: " + inputClass + " ->" + outputClass);
            });
        }
        catch (final ExecutionException e)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("No factory found with the given IO types: " + inputClass + " ->" + outputClass).initCause(e);
        }
    }

    @Override
    public <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final Class<? extends Input> clazz) throws IllegalArgumentException
    {
        final Set<Class> secondaryInputSet = ReflectionUtils.getSuperClasses(clazz);

        for (final Class secondaryInputClass : secondaryInputSet)
        {
            final IFactory factory = primaryInputMappings.get(secondaryInputClass);

            if (factory != null)
            {
                return factory;
            }
        }

        throw new IllegalArgumentException("The given input type is not a input of a factory.");
    }

    @Override
    public <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final Class<? extends Output> clazz) throws IllegalArgumentException
    {
        if (!primaryOutputMappings.containsKey(clazz))
        {
            if (!secondaryOutputMappings.containsKey(clazz))
            {
                throw new IllegalArgumentException("The given output type is not a output of a factory");
            }

            //Exists as the type exists in the secondary mapping. No specific output is requested, so we will take the first one.
            return secondaryOutputMappings.get(clazz).stream().findFirst().get();
        }

        return primaryOutputMappings.get(clazz);
    }

    @Override
    public <Input, Output> void registerNewFactory(@NotNull final IFactory<Input, Output> factory) throws IllegalArgumentException
    {
        if (primaryInputMappings.containsKey(factory.getFactoryInputType()))
        {
            throw new IllegalArgumentException("A factory with the given input type is already registered!");
        }

        if (primaryOutputMappings.containsKey(factory.getFactoryOutputType()))
        {
            throw new IllegalArgumentException("A factory with the given output type is already registered!");
        }

        Log.getLogger()
          .debug(
            "Registering factory: " + factory.toString() + " with input: " + factory.getFactoryInputType().toString() + " and output: " + factory.getFactoryOutputType() + ".");
        primaryInputMappings.put(factory.getFactoryInputType(), factory);
        primaryOutputMappings.put(factory.getFactoryOutputType(), factory);

        Log.getLogger()
          .debug("Retrieving super types of output: " + factory.getFactoryOutputType().toString());

        final Set<Class> outputSuperTypes = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());

        outputSuperTypes.remove(factory.getFactoryOutputType());

        if (!outputSuperTypes.isEmpty())
        {
            Log.getLogger().debug("Output type is not Object or Interface. Introducing secondary Output-Types");
            outputSuperTypes.forEach(t ->
            {
                if (!secondaryOutputMappings.containsKey(t))
                {
                    secondaryOutputMappings.put(t, new HashSet<>());
                }

                secondaryOutputMappings.get(t).add(factory);
            });
        }
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public <Output> NBTTagCompound serialize(@NotNull final Output object) throws IllegalArgumentException
    {
        final NBTTagCompound compound = new NBTTagCompound();

        final IFactory<?, Output> factory = getFactoryForOutput((Class<Output>) object.getClass());
        compound.setString(NBT_TYPE, object.getClass().getName());
        compound.setTag(NBT_DATA, factory.serialize(this, object));

        return compound;
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public <Output> Output deserialize(@NotNull final NBTTagCompound compound) throws IllegalArgumentException
    {
        final String className = compound.getString(NBT_TYPE);
        final Class<Output> outputClass;

        try
        {
            outputClass = (Class<Output>) Class.forName(className);
        }
        catch (final ClassNotFoundException e)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("The given compound holds an unknown output type for this Controller").initCause(e);
        }

        final IFactory<?, Output> factory = getFactoryForOutput(outputClass);
        return factory.deserialize(this, compound.getCompoundTag(NBT_DATA));
    }

    @Override
    public <Output> void writeToBuffer(@NotNull final ByteBuf buffer, @NotNull final Output object) throws IllegalArgumentException
    {
        NBTTagCompound bufferCompound = serialize(object);
        ByteBufUtils.writeTag(buffer, bufferCompound);
    }

    @Override
    public <Output> Output readFromBuffer(@NotNull final ByteBuf buffer) throws IllegalArgumentException
    {
        NBTTagCompound bufferCompound = ByteBufUtils.readTag(buffer);
        return deserialize(bufferCompound);
    }

    @Override
    public <Input, Output> Output getNewInstance(@NotNull final Input input, @NotNull final Object... context) throws IllegalArgumentException, ClassCastException
    {
        final Class outputClass = new TypeToken<Output>() {}.getRawType();
        final IFactory<Input, Output> factory = getFactoryForIO(input.getClass(), outputClass);

        return factory.getNewInstance(this, input, context);
    }

    @Override
    public <Output> Output getNewInstance() throws IllegalArgumentException
    {
        //Creating a new instance with VoidInput.
        return getNewInstance(FactoryVoidInput.INSTANCE);
    }
}
