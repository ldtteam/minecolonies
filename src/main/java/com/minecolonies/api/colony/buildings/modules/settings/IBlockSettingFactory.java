package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import net.minecraft.world.item.BlockItem;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for the boolean settings factory which is responsible for creating and maintaining bool setting objects.
 */
public interface IBlockSettingFactory<T extends ISetting> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < 2)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if (!(context[0] instanceof BlockItem))
        {
            throw new IllegalArgumentException("First parameter is supposed to be a BlockItem!");
        }

        if (!(context[1] instanceof BlockItem))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be a BlockItem!");
        }

        final BlockItem def = (BlockItem) context[0];
        final BlockItem current = (BlockItem) context[1];
        return getNewInstance(def, current);
    }

    @NotNull
    T getNewInstance(final BlockItem def, final BlockItem current);
}
