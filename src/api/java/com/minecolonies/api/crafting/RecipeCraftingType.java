package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link CraftingType} for the vanilla {@link RecipeType}
 * @param <C> the crafting inventory type
 * @param <T> the recipe type
 */
public class RecipeCraftingType<C extends Container, T extends Recipe<C>> extends CraftingType
{
    private final RecipeType<T> recipeType;
    private final Predicate<T> predicate;

    /**
     * Create a new instance
     * @param id the crafting type id
     * @param recipeType the vanilla recipe type
     * @param predicate filter acceptable recipes, or null to accept all
     */
    public RecipeCraftingType(@NotNull final ResourceLocation id,
                              @NotNull final RecipeType<T> recipeType,
                              @Nullable final Predicate<T> predicate)
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
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final T recipe : recipeManager.getAllRecipesFor(recipeType))
        {
            if (predicate != null && !predicate.test(recipe)) continue;

            tryAddingVanillaRecipe(recipes, recipe, world);
        }
        return recipes;
    }

    private static void tryAddingVanillaRecipe(@NotNull final List<IGenericRecipe> recipes,
                                               @NotNull final Recipe<?> recipe,
                                               @NotNull final Level world)
    {
        if (recipe.isSpecial() || recipe.getResultItem(world.registryAccess()).isEmpty()) return;     // invalid or special recipes
        try
        {
            final IGenericRecipe genericRecipe = GenericRecipe.of(recipe, world);
            if (genericRecipe == null || genericRecipe.getInputs().isEmpty()) return;
            recipes.add(genericRecipe);
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("Error evaluating recipe " + recipe.getId() + "; ignoring.", ex);
        }
    }
}
