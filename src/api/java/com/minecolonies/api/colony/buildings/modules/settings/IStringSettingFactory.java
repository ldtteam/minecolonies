package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for the enum settings factory which is responsible for creating and maintaining enum setting objects.
 */
public interface IStringSettingFactory<T extends ISetting> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < 2)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 is allowed!");
        }

        final List<String> settings = Arrays.asList((String[]) context[0]);
        final int current = (Integer) context[1];
        return getNewInstance(settings, current);
    }

    @NotNull
    T getNewInstance(final List<String> settings, final int current);
}
