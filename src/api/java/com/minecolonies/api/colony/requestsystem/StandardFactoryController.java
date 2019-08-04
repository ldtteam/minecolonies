package com.minecolonies.api.colony.requestsystem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.ITypeOverrideHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
    private static final StandardFactoryController     INSTANCE              = new StandardFactoryController();
    /**
     * Primary (main) INPUT mappings.
     */
    @NotNull
    private final        Map<TypeToken, Set<IFactory>> primaryInputMappings  = new HashMap<>();
    /**
     * Primary (main) OUTPUT mappings.
     */
    @NotNull
    private final        Map<TypeToken, Set<IFactory>> primaryOutputMappings = new HashMap<>();

    /**
     * Secondary (super) output mappings
     */
    @NotNull
    private final Map<TypeToken, Set<IFactory>>                secondaryOutputMappings = new HashMap<>();
    /**
     * A cache that holds all Mappers and their search secondary IO types.
     * Filled during runtime to speed up searches to factories when both INPUT and OUTPUT type are secondary types.
     */
    @NotNull
    private final Cache<Tuple<TypeToken, TypeToken>, IFactory> secondaryMappingsCache  = CacheBuilder.newBuilder().build();

    /**
     * List of the override handlers.
     */
    @NotNull
    private final List<ITypeOverrideHandler> typeOverrideHandlers = new ArrayList<>();

    /**
     * Map that handles class renamings during deserialization from older data.
     */
    @NotNull
    private final BiMap<String, String> classRenamingHandlers = HashBiMap.create();

    /**
     * Private constructor. Throws IllegalStateException if already created.
     *
     * We suppress warning squid:S2583 which makes sure that no null checks are executed on notnull fields.
     * In this case it makes sense since we need to make sure.
     *
     */
    @SuppressWarnings("squid:S2583")
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
    public <INPUT, OUTPUT> IFactory<INPUT, OUTPUT> getFactoryForIO(@NotNull final TypeToken<? extends INPUT> inputClass, @NotNull final TypeToken<? extends OUTPUT> outputClass) throws IllegalArgumentException
    {
        final ITypeOverrideHandler<?> inputOverrideHandler = typeOverrideHandlers.stream().filter(h -> h.matches(inputClass)).findFirst().orElse(null);
        final ITypeOverrideHandler<OUTPUT> outputOverrideHandler = typeOverrideHandlers.stream().filter(h -> h.matches(outputClass)).findFirst().orElse(null);

        final TypeToken input = inputOverrideHandler != null ? inputOverrideHandler.getOutputType() : inputClass;
        final TypeToken output = outputOverrideHandler != null ? outputOverrideHandler.getOutputType() : outputClass;
        try
        {
            //Request from cache or search.
            return secondaryMappingsCache.get(new Tuple<>(input, output), () ->
            {
                final Set<TypeToken> secondaryInputSet = ReflectionUtils.getSuperClasses(input);

                for (final TypeToken secondaryInputClass : secondaryInputSet)
                {
                    final Set<IFactory> factories = primaryInputMappings.get(secondaryInputClass);
                    if (factories == null || factories.isEmpty())
                    {
                        continue;
                    }

                    for (final IFactory factory : factories)
                    {
                        final Set<TypeToken> secondaryOutputSet = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());
                        if (secondaryOutputSet.contains(output))
                        {
                            return factory;
                        }
                    }
                }

                throw new IllegalArgumentException("No factory found with the given IO types: " + input + " ->" + output);
            });
        }
        catch (final ExecutionException e)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("No factory found with the given IO types: " + input + " ->" + output).initCause(e);
        }
    }

    @Override
    public <INPUT> IFactory<INPUT, ?> getFactoryForInput(@NotNull final TypeToken<? extends INPUT> inputClass) throws IllegalArgumentException
    {
        final ITypeOverrideHandler<?> inputOverrideHandler = typeOverrideHandlers.stream().filter(h -> h.matches(inputClass)).findFirst().orElse(null);

        final TypeToken input = inputOverrideHandler != null ? inputOverrideHandler.getOutputType() : inputClass;

        final Set<TypeToken> secondaryInputSet = ReflectionUtils.getSuperClasses(input);

        for (final TypeToken secondaryInputClass : secondaryInputSet)
        {
            final Set<IFactory> factories = primaryInputMappings.get(secondaryInputClass);

            if (factories != null && !factories.isEmpty())
            {
                return factories.stream().findFirst().get();
            }
        }

        throw new IllegalArgumentException("The given input type is not a input of a factory.");
    }

    @Override
    public <OUTPUT> IFactory<?, OUTPUT> getFactoryForOutput(@NotNull final TypeToken<? extends OUTPUT> outputClass) throws IllegalArgumentException
    {
        final ITypeOverrideHandler<OUTPUT> outputOverrideHandler = typeOverrideHandlers.stream().filter(h -> h.matches(outputClass)).findFirst().orElse(null);

        final TypeToken output = outputOverrideHandler != null ? outputOverrideHandler.getOutputType() : outputClass;

        if (!primaryOutputMappings.containsKey(output) || primaryOutputMappings.get(output).isEmpty())
        {
            if (!secondaryOutputMappings.containsKey(output))
            {
                throw new IllegalArgumentException("The given output type is not a output of a factory");
            }

            //Exists as the type exists in the secondary mapping. No specific output is requested, so we will take the first one.
            return secondaryOutputMappings.get(output).stream().findFirst().get();
        }

        return primaryOutputMappings.get(output).stream().findFirst().get();
    }

    @Override
    public <INPUT, OUTPUT> void registerNewFactory(@NotNull final IFactory<INPUT, OUTPUT> factory) throws IllegalArgumentException
    {
        primaryInputMappings.putIfAbsent(factory.getFactoryInputType(), new HashSet<>());
        primaryOutputMappings.putIfAbsent(factory.getFactoryOutputType(), new HashSet<>());

        final Set<IFactory> primaryInputFactories = primaryInputMappings.get(factory.getFactoryInputType());
        final Set<IFactory> primaryOutputFactories = primaryOutputMappings.get(factory.getFactoryOutputType());

        if (primaryInputFactories.contains(factory) || primaryOutputFactories.contains(factory))
        {
            throw new IllegalArgumentException("Cannot register the same factory twice!");
        }

        primaryInputFactories.add(factory);
        primaryOutputFactories.add(factory);

        final Set<TypeToken> outputSuperTypes = ReflectionUtils.getSuperClasses(factory.getFactoryOutputType());

        outputSuperTypes.remove(factory.getFactoryOutputType());

        if (!outputSuperTypes.isEmpty())
        {
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
    public <OUTPUT> CompoundNBT serialize(@NotNull final OUTPUT object) throws IllegalArgumentException
    {
        final CompoundNBT compound = new CompoundNBT();

        final IFactory<?, OUTPUT> factory = getFactoryForOutput((TypeToken<? extends OUTPUT>) TypeToken.of(object.getClass()));
        compound.putString(NBT_TYPE, object.getClass().getName());
        compound.put(NBT_DATA, factory.serialize(this, object));

        return compound;
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public <OUTPUT> OUTPUT deserialize(@NotNull final CompoundNBT compound) throws IllegalArgumentException
    {
        String className = compound.getString(NBT_TYPE);
        className = processClassRenaming(className);

        final IFactory<?, OUTPUT> factory;

        try
        {
            factory = getFactoryForOutput(className);
        }
        catch (final IllegalArgumentException e)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("The given compound holds an unknown output type for this Controller").initCause(e);
        }

        try
        {
            return factory.deserialize(this, compound.getCompound(NBT_DATA));
        }
        catch (Throwable throwable)
        {
            Log.getLogger().error(throwable);
            return null;
        }
    }

    private String processClassRenaming(@NotNull final String previousClassName)
    {
        if (!this.classRenamingHandlers.containsKey(previousClassName))
        {
            return previousClassName;
        }

        //See if we renamed something again.
        return processClassRenaming(this.classRenamingHandlers.get(previousClassName));
    }

    @Override
    public <OUTPUT> void writeToBuffer(@NotNull final PacketBuffer buffer, @NotNull final OUTPUT object) throws IllegalArgumentException
    {
        final CompoundNBT bufferCompound = serialize(object);
        buffer.writeCompoundTag(bufferCompound);
    }

    @Override
    public <OUTPUT> OUTPUT readFromBuffer(@NotNull final PacketBuffer buffer) throws IllegalArgumentException
    {
        final CompoundNBT bufferCompound = buffer.readCompoundTag();
        return deserialize(bufferCompound);
    }

    @Override
    public <INPUT, OUTPUT> OUTPUT getNewInstance(@NotNull final TypeToken<? extends OUTPUT> requestedType, @NotNull final INPUT input, @NotNull final Object... context)
      throws IllegalArgumentException, ClassCastException
    {
        final TypeToken<? extends INPUT> inputToken = TypeToken.of((Class<? extends INPUT>) input.getClass());
        final IFactory<INPUT, OUTPUT> factory = getFactoryForIO(inputToken, requestedType);

        return factory.getNewInstance(this, input, context);
    }

    @Override
    public <OUTPUT> OUTPUT getNewInstance(@NotNull final TypeToken<? extends OUTPUT> requestedType) throws IllegalArgumentException
    {
        //Creating a new instance with VoidInput.
        return getNewInstance(requestedType, FactoryVoidInput.INSTANCE);
    }

    @Override
    public <OUTPUT> void registerNewTypeOverrideHandler(@NotNull final ITypeOverrideHandler<OUTPUT> overrideHandler)
    {
        this.typeOverrideHandlers.add(overrideHandler);
    }

    @Override
    public void registerNewClassRenaming(@NotNull final String previousName, @NotNull final String newName)
    {
        this.classRenamingHandlers.put(previousName, newName);
    }
}
