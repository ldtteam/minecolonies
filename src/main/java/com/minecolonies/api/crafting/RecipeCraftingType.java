package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.Log;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link CraftingType} for the vanilla {@link RecipeType}
 * @param <C> the crafting inventory type
 * @param <T> the recipe type
 */
public class RecipeCraftingType<C extends RecipeInput, T extends Recipe<C>> extends CraftingType
{
    /**
     * Minecraft 26.2 moved grid sizing from {@link Recipe} to concrete recipes.
     * Shapeless and custom recipes remain usable in either grid.
     */
    public static boolean canCraftInDimensions(final Recipe<?> recipe, final int width, final int height)
    {
        if (recipe instanceof final ShapedRecipe shapedRecipe)
        {
            return shapedRecipe.getWidth() <= width && shapedRecipe.getHeight() <= height;
        }
        return true;
    }

    private final RecipeType<T> recipeType;
    private final Predicate<Recipe<?>> predicate;

    /**
     * Create a new instance
     * @param id the crafting type id
     * @param recipeType the vanilla recipe type
     * @param predicate filter acceptable recipe values, or null to accept all
     */
    public RecipeCraftingType(@NotNull final Identifier id,
                              @NotNull final RecipeType<T> recipeType,
                              @Nullable final Predicate<Recipe<?>> predicate)
    {
        super(id);
        this.recipeType = recipeType;
        this.predicate = predicate;
    }

    @Override
    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull RecipeManager recipeManager,
                                            @NotNull final Level world)
    {
        return findRecipes(recipeManager.getRecipes(), world);
    }

    @Override
    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull final Collection<RecipeHolder<?>> recipeHolders,
                                            @NotNull final Level world)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final RecipeHolder<?> holder : recipeHolders)
        {
            if (!holder.value().getType().equals(recipeType)) continue;
            if (predicate != null && !predicate.test(holder.value())) continue;

            tryAddingVanillaRecipe(recipes, holder, world);
        }
        return recipes;
    }

    private void tryAddingVanillaRecipe(@NotNull final List<IGenericRecipe> recipes,
                                               @NotNull final RecipeHolder<?> holder,
                                               @NotNull final Level world)
    {
        final Recipe<?> recipe = holder.value();
        // Only recipes with usable placement metadata and a displayable
        // output can be projected into MineColonies' generic recipe format.
        // Special recipes and recipes without a placement contract remain
        // outside the generic projection.
        if (recipe.isSpecial() || RecipeUtils.getIngredients(recipe).isEmpty()) return;
        if (RecipeUtils.getOutput(recipe, world).isEmpty()) return;     // invalid or unsupported recipes
        try
        {
            final IGenericRecipe genericRecipe = GenericRecipe.of(holder, world);
            if (genericRecipe == null || genericRecipe.getInputs().isEmpty()) return;
            recipes.add(genericRecipe);
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Error evaluating recipe " + holder.id() + "; ignoring.", ex);
        }
    }
}
