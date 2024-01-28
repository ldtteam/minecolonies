package com.minecolonies.api.colony.requestsystem.factory;

import com.google.common.reflect.TypeToken;

public interface ITypeOverrideHandler<O>
{
    /**
     * Method used if this typeoverride handler is used to override a given type.
     *
     * @param inputType The type to check
     * @return true when this handler overrides the given type, false when not.
     */
    boolean matches(TypeToken<?> inputType);

    /**
     * Method used to get the EXACT type result.
     *
     * @return The exact type that this {@link ITypeOverrideHandler} converts to.
     */
    TypeToken<O> getOutputType();
}
