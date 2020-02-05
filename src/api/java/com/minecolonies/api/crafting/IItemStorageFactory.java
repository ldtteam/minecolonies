package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.item.ItemStack;
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

        if(!(context[0] instanceof ItemStack))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an ItemStack!");
        }

        if(!(context[1] instanceof Integer))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be an Integer!");
        }

        final ItemStack stack = (ItemStack) context[0];
        final int size = (int) context[1];
        return getNewInstance(stack, size);
    }

    /**
     * Method to get a new Instance of an itemStorage.
     * @param stack the input.
     * @param size the grid size.
     * @return a new Instance of ItemStorage.
     */
    @NotNull
    ItemStorage getNewInstance(@NotNull final ItemStack stack, final int size);
}

