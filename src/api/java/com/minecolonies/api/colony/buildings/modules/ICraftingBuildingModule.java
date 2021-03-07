package com.minecolonies.api.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import net.minecraft.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * This module represents the ability for a building to generate
 * items via some form of crafting, whether vanilla, smelter, or
 * some entirely custom process.  It also supports (within some
 * restrictions) automatic JEI integration to let the player look
 * up which items can be produced at each building.
 *
 * A single building may have more than one of these, but in that
 * case they must be linked to different jobs (or to no job) and
 * you have to more carefully manage the learned recipe list.
 */
public interface ICraftingBuildingModule extends IBuildingModule
{
    /**
     * Gets the crafting job associated with this building type.
     * This might not be the primary job of the building.
     *
     * Note that this must either always return null or always
     * return a valid job, even if this module is not currently
     * associated with a specific colony or building.
     *
     * It may either return the actual job associated with a
     * citizen working at the current building, or an abstract
     * job not yet associated with any particular citizen.
     *
     * Every unique module (across all buildings) must return a
     * unique job -- it is not permitted for the same job to be
     * shared by two modules, as that creates an ambiguity.
     *
     * @return The crafting job, or null if there is no such job.
     */
    @Nullable
    IJob<?> getCraftingJob();

    /**
     * Check if this building type can learn (or otherwise process)
     * vanilla crafting recipes of some kind.
     *
     * @return True if so; false otherwise.
     */
    boolean canLearnCraftingRecipes();

    /**
     * Check if this building type can learn (or otherwise process)
     * vanilla smelting recipes of some kind.
     *
     * @return True if so; false otherwise.
     */
    boolean canLearnFurnaceRecipes();

    /**
     * Check if it is possible for this building type to learn
     * recipes at *some* level.
     *
     * @param rightNow When true, checks the current building
     *                 level and if the recipe list is full.
     *                 When false, doesn't check these.
     * @return True if recipes can be taught.
     */
    boolean canLearnRecipes(boolean rightNow);

    /**
     * Check if it is possible for this building type to learn
     * recipes that will only fit in a 3x3 crafting grid.
     *
     * @return True if 3x3 recipes can be taught.
     */
    boolean canLearnLargeRecipes();

    /**
     * Checks if this particular recipe is *possible* to be learned by
     * this building (or otherwise possible to be crafted there).
     *
     * This is checked without regard to specific colony or level of
     * building, or whether there are spare recipe slots or not.
     *
     * @param recipe The recipe to check.
     * @return True if this recipe could be produced/taught.
     */
    boolean isRecipeCompatible(@NotNull IGenericRecipe recipe);

    /**
     * Generates any additional special recipes supported by this
     * crafter.
     *
     * @param vanilla The vanilla recipes; can optionally be used
     *                to help generate special recipes.
     */
    @NotNull
    default List<IRecipeStorage> getAdditionalRecipes(@NotNull final RecipeManager vanilla) { return Collections.emptyList(); }

    @NotNull
    default List<IGenericRecipe> getPossibleRecipesForDisplayPurposesOnly(@NotNull final IJob<?> job)
    {
        return job.getPossibleRecipesForDisplayPurposesOnly();
    }

    /**
     * Tries to teach this specific recipe to this specific building.
     * Can fail if there is no associated building, or the building is
     * already full or too low level for this recipe.
     *
     * @param storage The recipe to try to learn.
     * @return True if the recipe was successfully learned.
     */
    //boolean tryLearningRecipe(@NotNull final IRecipeStorage storage);
}
