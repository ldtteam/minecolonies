package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.minecolonies.api.util.constant.Constants.MAX_PARAMS_IRECIPESTORAGE;
import static com.minecolonies.api.util.constant.Constants.MIN_PARAMS_IRECIPESTORAGE;

/**
 * Interface for the IRecipeStorageFactory which is responsible for creating and maintaining RecipeStorage objects.
 */
public interface IRecipeStorageFactory extends IFactory<IToken<?>, RecipeStorage>
{
    @NotNull
    @Override
    default RecipeStorage getNewInstance(@NotNull final IFactoryController factoryController,
        @NotNull final IToken<?> token,
        @NotNull final Object... context)
    {
        if (context.length < MIN_PARAMS_IRECIPESTORAGE || context.length > MAX_PARAMS_IRECIPESTORAGE)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. At least 3 at max 4 are needed.!");
        }

        if (!(context[0] instanceof List))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an Arraylist!");
        }

        if (!(context[1] instanceof Integer))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be an Integer!");
        }

        if (!(context[2] instanceof ItemStack))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be an ItemStack!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE && !(context[MAX_PARAMS_IRECIPESTORAGE - 1] instanceof Block))
        {
            throw new IllegalArgumentException("Forth parameter is supposed to be a Block or Null!");
        }

        final List<ItemStack> input = (List<ItemStack>) context[0];
        final int gridSize = (int) context[1];
        final ItemStack primaryOutput = (ItemStack) context[2];
        final Block intermediate = context.length < 4 ? null : (Block) context[3];
        return getNewInstance(token, input, gridSize, primaryOutput, intermediate);
    }

    /**
     * Method to get a new Instance of an recipe.
     * 
     * @param token         the token of it.
     * @param input         the input.
     * @param gridSize      the grid size.
     * @param primaryOutput the primary output.
     * @param intermediate  the intermediate.
     * @return a new Instance of IRecipeStorage.
     */
    @NotNull
    RecipeStorage getNewInstance(@NotNull final IToken<?> token,
        @NotNull final List<ItemStack> input,
        final int gridSize,
        @NotNull final ItemStack primaryOutput,
        @Nullable final Block intermediate);
}
