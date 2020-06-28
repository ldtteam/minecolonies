package com.minecolonies.api.colony.requestsystem.factory;

/**
 * This class is used when a Factory does not require any input to produce an output.
 */
public final class FactoryVoidInput
{

    public static final FactoryVoidInput INSTANCE = new FactoryVoidInput();

    private FactoryVoidInput()
    {
        //NOOP
    }
}
