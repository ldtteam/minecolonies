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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
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
    private static final StandardFactoryController    INSTANCE              = new StandardFactoryController();
    /**
     * Primary (main) Input mappings.
     */
    @NotNull
    private final        Map<TypeToken, IFactory>     primaryInputMappings  = new HashMap<>();
    /**
     * Primary (main) Output mappings.
     */
    @NotNull
    private final        Map<TypeToken, IFactory> primaryOutputMappings = new HashMap<>();

    /**
     * Secondary (super) output mappings
     */
    @NotNull
    private final Map<TypeToken, Set<IFactory>>            secondaryOutputMappings = new HashMap<>();
    /**
     * A cache that holds all Mappers and their search secondary IO types.
     * Filled during runtime to speed up searches to factories when both Input and Output type are secondary types.
     */
    @NotNull
    private final Cache<Tuple<TypeToken, TypeToken>, IFactory> secondaryMappingsCache  = CacheBuilder.newBuilder().build();

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
    public <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final TypeToken<Input> inputTypeToken) throws IllegalArgumentException
    {
        final Set<TypeToken> secondaryInputSet = ReflectionUtils.getSuperClasses(inputTypeToken);

        for (final TypeToken token : secondaryInputSet)
        {
            final IFactory factory = primaryInputMappings.get(token);

            if (factory != null)
            {
                return factory;
            }
        }

        throw new IllegalArgumentException("The given input type is not a input of a factory.");
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    @Override
    public <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException
    {
        if (!primaryOutputMappings.containsKey(outputTypeToken))
        {
            if (!secondaryOutputMappings.containsKey(outputTypeToken))
            {
                throw new IllegalArgumentException("The given output type is not a output of a factory");
            }

            //Exists as the type exists in the secondary mapping. No specific output is requested, so we will take the first one.
            return secondaryOutputMappings.get(outputTypeToken).stream().findFirst().get();
        }

        return primaryOutputMappings.get(outputTypeToken);
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    @Override
    public <Input, Output> IFactory<Input, Output> getFactoryForIO(@NotNull final TypeToken<Input> inputTypeToken, @NotNull final TypeToken<Output> outputTypeToken)
      throws IllegalArgumentException
    {
        try
        {
            //Request from cache or search.
            return secondaryMappingsCache.get(new Tuple<>(inputTypeToken, outputTypeToken), () ->
            {
                Log.getLogger().debug("Attempting to find a Factory with Primary: " + inputTypeToken.toString() + " -> " + outputTypeToken.toString());

                final Set<TypeToken> secondaryInputSet = ReflectionUtils.getSuperClasses(inputTypeToken);

                for (final TypeToken token : secondaryInputSet)
                {
                    final IFactory factory = primaryInputMappings.get(token);
                    if (factory == null)
                    {
                        continue;
                    }

                    Log.getLogger().debug("Found matching Factory for Primary input type.");
                    final Set<TypeToken> secondaryOutputSet = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());
                    if (secondaryOutputSet.contains(outputTypeToken))
                    {
                        Log.getLogger().debug("Found input factory with matching super Output type. Search complete with: " + factory);
                        return factory;
                    }
                }

                throw new IllegalArgumentException("No factory found with the given IO types: " + inputTypeToken + " ->" + outputTypeToken);
            });
        }
        catch (final ExecutionException e)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("No factory found with the given IO types: " + inputTypeToken + " ->" + outputTypeToken).initCause(e);
        }
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

        final Set<TypeToken> outputSuperTypes = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());

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

        final IFactory<?, Output> factory = getFactoryForOutput(TypeToken.of((Class<Output>) object.getClass()));
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

        final IFactory<?, Output> factory = getFactoryForOutput(TypeToken.of(outputClass));
        return factory.deserialize(this, compound.getCompoundTag(NBT_DATA));
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public <Input, Output> Output getNewInstance(@NotNull final Input input, @NotNull final TypeToken<Output> outputTypeToken, @NotNull final Object... context)
      throws IllegalArgumentException, ClassCastException
    {
        final IFactory<Input, Output> factory = getFactoryForIO(TypeToken.of((Class<Input>) input.getClass()), outputTypeToken);

        return factory.getNewInstance(input, context);
    }

    @Override
    public <Output> Output getNewInstance(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException
    {
        //Creating a new instance with VoidInput.
        return getNewInstance(FactoryVoidInput.getInstance(), outputTypeToken);
    }
}
