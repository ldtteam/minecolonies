package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.PARAMS_ITEMSTORAGE;

/**
 * Interface for the IItemStorageFactory which is responsible for creating and maintaining ItemStorage objects.
 */
public interface IItemStorageFactory extends IFactory<FactoryVoidInput, ItemStorage>
{
    @NotNull
    @Override
    default ItemStorage getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < PARAMS_ITEMSTORAGE)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if (!(context[0] instanceof ItemStack))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an ItemStack!");
        }

        if (!(context[1] instanceof Integer))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be an Integer!");
        }

        if (context.length >= PARAMS_ITEMSTORAGE + 1 && !(context[2] instanceof Boolean))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be an Boolean!");
        }

        if (context.length >= PARAMS_ITEMSTORAGE + 2 && !(context[3] instanceof Boolean))
        {
            throw new IllegalArgumentException("Fourth parameter is supposed to be an Boolean!");
        }

        final ItemStack stack = (ItemStack) context[0];
        final int size = (int) context[1];
        final boolean ignoreDamage = context.length >= 3 ? (Boolean) context[2] : false;
        final boolean ignoreNBT = context.length >= 4 ? (Boolean) context[3] : false;

        return getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    /**
     * Method to get a new Instance of an itemStorage.
     *
     * @param stack the input.
     * @param size  the grid size.
     * @return a new Instance of ItemStorage.
     */
    @NotNull
    ItemStorage getNewInstance(@NotNull final ItemStack stack, final int size, final boolean ignoreDamage, final boolean ignoreNBT);
}

