package com.minecolonies.api.colony.requestsystem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ReflectionUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of a FactoryController
 * Singleton.
 */
public class StandardFactoryController implements IFactoryController
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    static final String NBT_TYPE = "Type";
    static final String NBT_DATA = "Data";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Instance variable.
     */
    private static final StandardFactoryController                    INSTANCE                = new StandardFactoryController();
    /**
     * Primary (main) Input mappings.
     */
    @NotNull
    private final        HashMap<TypeToken, IFactory>                 primaryInputMappings    = new HashMap<>();
    /**
     * Primary (main) Output mappings.
     */
    @NotNull
    private final        HashMap<TypeToken, IFactory>                 primaryOutputMappings   = new HashMap<>();
    /**
     * Secondary (super) Input mappings
     */
    @NotNull
    private final        HashMap<TypeToken, Set<IFactory>>            secondaryInputMappings  = new HashMap<>();
    /**
     * Secondary (super) output mappings
     */
    @NotNull
    private final        HashMap<TypeToken, Set<IFactory>>            secondaryOutputMappings = new HashMap<>();
    /**
     * A cache that holds all Mappers and their search secondary IO types.
     * Filled during runtime to speed up searches to factories when both Input and Output type are secondary types.
     */
    @NotNull
    private final        Cache<Tuple<TypeToken, TypeToken>, IFactory> secondaryMappingsCache  = CacheBuilder.newBuilder().build();


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
     * Resets the FactoryController to the default values.
     * Clears all registered Factories.
     *
     * Only used for testing.
     */
    public static void reset()
    {
        getInstance().primaryInputMappings.clear();
        getInstance().primaryOutputMappings.clear();
        getInstance().secondaryInputMappings.clear();
        getInstance().secondaryOutputMappings.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Input> IFactory<Input, ?> getFactoryForInput(@NotNull final TypeToken<Input> inputTypeToken) throws IllegalArgumentException
    {
        if (!primaryInputMappings.containsKey(inputTypeToken))
        {
            if (!secondaryInputMappings.containsKey(inputTypeToken))
            {
                throw new IllegalArgumentException("The given input type is not a Input of a factory");
            }

            //Exists as the type exists in the secondary mapping. No specific output is requested, so we will take the first one.
            return secondaryInputMappings.get(inputTypeToken).stream().findFirst().get();
        }

        return primaryInputMappings.get(inputTypeToken);
    }
    
    @Override
    public <Output> IFactory<?, Output> getFactoryForOutput(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException
    {
        if (!primaryOutputMappings.containsKey(outputTypeToken))
        {
            if (!secondaryOutputMappings.containsKey(outputTypeToken))
            {
                throw new IllegalArgumentException("The given output type is not a Input of a factory");
            }

            //Exists as the type exists in the secondary mapping. No specific output is requested, so we will take the first one.
            return secondaryOutputMappings.get(outputTypeToken).stream().findFirst().get();
        }

        return primaryOutputMappings.get(outputTypeToken);
    }

    @SuppressWarnings("unchecked")
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
                if (primaryInputMappings.containsKey(inputTypeToken))
                {
                    Log.getLogger().debug("Found matching Factory for Primary input type.");
                    IFactory<Input, ?> inputMatchingFactory = primaryInputMappings.get(inputTypeToken);

                    if (ReflectionUtils.getSuperClasses(inputMatchingFactory.getFactoryOutputType()).contains(outputTypeToken))
                    {
                        Log.getLogger().debug("Found input factory with matching super Output type. Search complete with: " + inputMatchingFactory);
                        return inputMatchingFactory;
                    }

                    Log.getLogger().debug("Found input factory is invalid, attempting with Primary Output type.");
                }
                else
                {
                    Log.getLogger().debug("No factory found with matching primary input type.");
                }

                if (primaryOutputMappings.containsKey(outputTypeToken))
                {
                    Log.getLogger().debug("Found matching Factory for Primary output type.");
                    IFactory<Output, ?> outputMatchingFactory = primaryOutputMappings.get(outputTypeToken);

                    if (ReflectionUtils.getSuperClasses(outputMatchingFactory.getFactoryOutputType()).contains(outputTypeToken))
                    {
                        Log.getLogger().debug("Found output factory with matching super Input type. Search complete with: " + outputMatchingFactory);
                        return outputMatchingFactory;
                    }

                    Log.getLogger().debug("Found output factory is invalid, ");
                }
                else
                {
                    Log.getLogger().debug("No factory found with matching primary output type.");
                }

                Log.getLogger().debug("Failed to find factory with either primary Input or Output type.");
                Log.getLogger().debug("Attempting search for matching secondary input or output type");

                if (secondaryInputMappings.containsKey(inputTypeToken) && secondaryOutputMappings.containsKey(outputTypeToken))
                {
                    Log.getLogger().debug("Found factories with matching secondary Input type.");
                    Set<IFactory> secondaryInputFactories = secondaryInputMappings.get(inputTypeToken);

                    Log.getLogger().debug("Attempting to find mathing secondary Output type:");
                    @Nullable final IFactory possibleMatchingFactory =
                      secondaryInputFactories.stream().filter(secondaryOutputMappings.get(outputTypeToken)::contains).findFirst().orElse(null);

                    if (possibleMatchingFactory != null)
                    {
                        Log.getLogger().debug("Found matching factory with secondary input and output type.");
                        return possibleMatchingFactory;
                    }
                }

                throw new IllegalArgumentException("No factory found with the given IO types: " + inputTypeToken + " ->" + outputTypeToken);
            });
        }
        catch (ExecutionException e)
        {
            throw new IllegalArgumentException("No factory found with the given IO types: " + inputTypeToken + " ->" + outputTypeToken);
        }
    }

    @Override
    public <Input, Output> void registerNewFactory(@NotNull IFactory<Input, Output> factory) throws IllegalArgumentException
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
          .debug("Retrieving super types of input: " + factory.getFactoryInputType().toString() + " and output: " + factory.getFactoryOutputType().toString());

        Set<TypeToken> inputSuperTypes = ReflectionUtils.getSuperClasses(factory.getFactoryInputType());
        Set<TypeToken> outputSuperTypes = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());

        inputSuperTypes.remove(factory.getFactoryInputType());
        outputSuperTypes.remove(factory.getFactoryOutputType());

        if (inputSuperTypes.size() > 0)
        {
            Log.getLogger().debug("Input type is not Object or Interface. Introducing secondary Input-Types.");

            inputSuperTypes.forEach(t ->
            {
                if (!secondaryInputMappings.containsKey(t))
                {
                    secondaryInputMappings.put(t, new HashSet<>());
                }

                secondaryInputMappings.get(t).add(factory);
            });
        }

        if (outputSuperTypes.size() > 0)
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
    @SuppressWarnings("unchecked")
    public <Output> NBTTagCompound serialize(@NotNull Output object) throws IllegalArgumentException
    {
        NBTTagCompound compound = new NBTTagCompound();

        IFactory<?, Output> factory = getFactoryForOutput(TypeToken.of((Class<Output>) object.getClass()));
        compound.setString(NBT_TYPE, object.getClass().getName());
        compound.setTag(NBT_DATA, factory.serialize(this, object));

        return compound;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Output> Output deserialize(@NotNull NBTTagCompound compound) throws IllegalArgumentException
    {
        String className = compound.getString(NBT_TYPE);
        Class<Output> outputClass;

        try
        {
            outputClass = (Class<Output>) Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("The given compound holds an unknown output type for this Controller");
        }

        IFactory<?, Output> factory = getFactoryForOutput(TypeToken.of(outputClass));
        return factory.deserialize(this, compound.getCompoundTag(NBT_DATA));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Input, Output> Output getNewInstance(@NotNull final Input input, @NotNull final TypeToken<Output> outputTypeToken, @NotNull Object... context)
      throws IllegalArgumentException, ClassCastException
    {
        IFactory<Input, Output> factory = getFactoryForIO(TypeToken.of((Class<Input>) input.getClass()), outputTypeToken);

        return factory.getNewInstance(input, context);
    }

    @Override
    public <Output> Output getNewInstance(@NotNull final TypeToken<Output> outputTypeToken) throws IllegalArgumentException
    {
        //Creating a new instance with VoidInput.
        return getNewInstance(FactoryVoidInput.getInstance(), outputTypeToken);
    }
}
