package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
    default RecipeStorage getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final IToken<?> token, @NotNull final Object... context)
    {
        if (context.length < MIN_PARAMS_IRECIPESTORAGE || context.length > MAX_PARAMS_IRECIPESTORAGE)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. At least 3 at max 5 are needed.!");
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

        if (context.length > MIN_PARAMS_IRECIPESTORAGE && context[3] != null && !(context[3] instanceof Block))
        {
            throw new IllegalArgumentException("Fourth parameter is supposed to be a Block or Null!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE + 1 && context[4] != null && !(context[4] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Fifth parameter is supposed to be a ResourceLocation or Null!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE + 2 && context[5] != null && !(context[5] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Sixth parameter is supposed to be a ResourceLocation or Null!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE + 3 && context[6] != null && !(context[6] instanceof List))
        {
            throw new IllegalArgumentException("Seventh parameter is supposed to be a List<ItemStack> or Null!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE + 4 && context[7] != null && !(context[7] instanceof List))
        {
            throw new IllegalArgumentException("Eighth parameter is supposed to be a List<ItemStack> or Null!");
        }

        if (context.length > MIN_PARAMS_IRECIPESTORAGE + 5 && context[8] != null && !(context[8] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Ninth parameter is supposed to be a ResourceLocation or Null!");
        }

        final List<ItemStack> input = (List<ItemStack>) context[0];
        final int gridSize = (int) context[1];
        final ItemStack primaryOutput = (ItemStack) context[2];
        final Block intermediate = context.length < 4 ? null : (Block) context[3];
        final ResourceLocation source = context.length < 5 ? null : (ResourceLocation) context[4];
        final ResourceLocation type  = context.length < 6 ? null : (ResourceLocation) context[5];
        final List<ItemStack> altOutputs = context.length < 7 ? null :  (List<ItemStack>) context[6];
        final List<ItemStack> secOutputs = context.length < 8 ? null :  (List<ItemStack>) context[7];
        final ResourceLocation lootTable = context.length < 9 ? null : (ResourceLocation) context[8];
        return getNewInstance(token, input, gridSize, primaryOutput, intermediate, source, type, altOutputs, secOutputs, lootTable);
    }

    /**
     * Method to get a new Instance of an recipe.
     *
     * @param token         the token of it.
     * @param input         the input.
     * @param gridSize      the grid size.
     * @param primaryOutput the primary output.
     * @param intermediate  the intermediate.
     * @param source        the source of this recipe, either a registry name or the player name
     * @param type          What type this recipe is, classic or multi-recipe
     * @param altOutputs    possible alternate outputs other than the primaryOutput
     * @param secOutputs    Leave-behind items in the grid. ie: bucket, pot, juicer, or hammer
     * @return a new Instance of IRecipeStorage.
     */
    @NotNull
    RecipeStorage getNewInstance(
      @NotNull final IToken<?> token,
      @NotNull final List<ItemStack> input,
      final int gridSize,
      @NotNull final ItemStack primaryOutput,
      @Nullable final Block intermediate,
      @Nullable final ResourceLocation source,
      @Nullable final ResourceLocation type,
      @Nullable final List<ItemStack> altOutputs,
      @Nullable final List<ItemStack> secOutputs,
      @Nullable final ResourceLocation lootTable
      );
}

