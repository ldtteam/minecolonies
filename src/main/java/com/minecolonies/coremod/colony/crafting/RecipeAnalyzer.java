package com.minecolonies.coremod.colony.crafting;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers for analyzing the available recipes and determining which crafters are able to use them.
 */
public final class RecipeAnalyzer
{
    /**
     * Build a map of all potentially learnable vanilla recipes, converted to {@link IGenericRecipe}.
     *
     * @param recipeManager the vanilla recipe manager
     * @return the recipe map
     */
    public static Map<CraftingType, List<IGenericRecipe>> buildVanillaRecipesMap(@NotNull final RecipeManager recipeManager,
                                                                                 @Nullable final World world)
    {
        final ImmutableMap.Builder<CraftingType, List<IGenericRecipe>> builder = ImmutableMap.builder();

        for (final CraftingType type : MinecoloniesAPIProxy.getInstance().getCraftingTypeRegistry().getValues())
        {
            final List<IGenericRecipe> recipes = type.findRecipes(recipeManager, world);
            builder.put(type, recipes);
        }

        return builder.build();
    }

    /**
     * Find all recipes for a given crafter.
     *
     * @param vanilla vanilla recipes map.
     * @param crafting crafting module.
     * @return list of recipes
     */
    @NotNull
    public static List<IGenericRecipe> findRecipes(@NotNull final Map<CraftingType, List<IGenericRecipe>> vanilla,
                                                   @NotNull final ICraftingBuildingModule crafting)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();

        // all vanilla teachable recipes
        for (final Map.Entry<CraftingType, List<IGenericRecipe>> entry : vanilla.entrySet())
        {
            if (crafting.canLearn(entry.getKey()))
            {
                for (final IGenericRecipe recipe : entry.getValue())
                {
                    final IGenericRecipe safeRecipe = GenericRecipeUtils.filterInputs(recipe, crafting.getIngredientValidator());
                    if (crafting.isRecipeCompatible(safeRecipe))
                    {
                        recipes.add(safeRecipe);
                    }
                }
            }
        }

        // custom MineColonies additional recipes
        for (final CustomRecipe customRecipe : CustomRecipeManager.getInstance().getRecipes(crafting.getCustomRecipeKey()))
        {
            final IRecipeStorage recipeStorage = customRecipe.getRecipeStorage();
            if (!recipeStorage.getAlternateOutputs().isEmpty())
            {
                // this is a multi-output recipe; assume it replaces a bunch of vanilla
                // recipes we already added above
                recipes.removeIf(r -> ItemStackUtils.compareItemStacksIgnoreStackSize(recipeStorage.getPrimaryOutput(), r.getPrimaryOutput()));
                recipes.removeIf(r -> recipeStorage.getAlternateOutputs().stream()
                        .anyMatch(s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, r.getPrimaryOutput())));
            }
            recipes.add(GenericRecipeUtils.create(customRecipe, recipeStorage));
        }

        // and even more recipes that can't be taught, but are just inherent in the worker AI
        recipes.addAll(crafting.getAdditionalRecipesForDisplayPurposesOnly());

        return recipes;
    }

    private RecipeAnalyzer()
    {
        /*
         * Intentionally left empty.
         */
    }
}
