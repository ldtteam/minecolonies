package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;

/**
 * This class is used when a Factory does not require any input to produce an output.
 *
 * @param <Output> The type of Output that a {@link IFactory} produces when given a Void input.
 */
public final class FactoryVoidInput<Output>
{

    private final TypeToken<Output> outputTypeToken = new TypeToken<Output>() {};

    private FactoryVoidInput()
    {
        //NOOP
    }

    public static <Output> FactoryVoidInput<Output> getInstance()
    {
        return new FactoryVoidInput<>();
    }

    /**
     * Method to get the Output type that is produced when
     *
     * @return The output type that should be produced when this void input is passed into a factory.
     */
    public TypeToken<Output> getOutputTypeToken()
    {
        return outputTypeToken;
    }
}
