package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for the integer settings factory which is responsible for creating and maintaining int setting objects.
 */
public interface IIntSettingFactory<T extends ISetting> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < 2)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if (!(context[0] instanceof Integer))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an Integer!");
        }

        if (!(context[1] instanceof Integer))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be an Integer!");
        }

        final int def = (Integer) context[0];
        final int current = (Integer) context[1];
        return getNewInstance(def, current);
    }

    @NotNull
    T getNewInstance(final int def, final int current);
}
