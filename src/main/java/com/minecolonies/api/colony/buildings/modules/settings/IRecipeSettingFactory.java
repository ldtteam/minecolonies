package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for the recipe setting factory.
 */
public interface IRecipeSettingFactory<T extends ISetting> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < 2)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if (!(context[0] instanceof IToken))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an IToken<?>!");
        }

        if (!(context[1] instanceof String))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be a String!");
        }

        final IToken<?> index = (IToken<?>) context[0];
        final String module = (String) context[1];
        return getNewInstance(index, module);
    }

    @NotNull
    T getNewInstance(final IToken<?> def, final String current);
}
