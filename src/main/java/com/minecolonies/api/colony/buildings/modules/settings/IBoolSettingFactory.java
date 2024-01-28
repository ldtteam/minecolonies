package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for the boolean settings factory which is responsible for creating and maintaining bool setting objects.
 */
public interface IBoolSettingFactory<T extends ISetting> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < 2)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if (!(context[0] instanceof Boolean))
        {
            throw new IllegalArgumentException("First parameter is supposed to be a Boolean!");
        }

        if (!(context[1] instanceof Boolean))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be a Boolean!");
        }

        final boolean def = (Boolean) context[0];
        final boolean current = (Boolean) context[1];
        return getNewInstance(def, current);
    }

    @NotNull
    T getNewInstance(final boolean def, final boolean current);
}
