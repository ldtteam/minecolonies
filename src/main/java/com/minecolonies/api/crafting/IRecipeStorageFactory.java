package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for the IRecipeStorageFactory which is responsible for creating and maintaining RecipeStorage objects.
 */
public interface IRecipeStorageFactory extends IFactory<IToken<?>, RecipeStorage>
{
    @NotNull
    @Override
    default RecipeStorage getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final IToken<?> token, @NotNull final Object... context)
    {
        throw new NotImplementedException();    // use RecipeStorage.builder() instead
    }
}

