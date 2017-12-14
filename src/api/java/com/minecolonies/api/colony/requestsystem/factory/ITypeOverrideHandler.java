package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;

public interface ITypeOverrideHandler<I, O>
{
    /**
     * Method used to get the EXACT type that is being overriden.
     *
     * @return The exact type that this {@link ITypeOverrideHandler} converts.
     */
    TypeToken<I> getInputType();

    /**
     * Method used to get the EXACT type result.
     *
     * @return The exact type that this {@link ITypeOverrideHandler} converts to.
     */
    TypeToken<O> getOutputType();
}
