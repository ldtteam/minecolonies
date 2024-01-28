package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.OptionalPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

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
 *
 * Note that (somewhat uniquely among modules) while there is a
 * "real" one of these attached to the building on the server
 * side that can hold state, additional instances can also be
 * created (with a null building) on the client side in order to
 * answer abstract (but building-type-specific) questions for JEI.
 * When called this way, it must be able to provide answers
 * completely stateless with no expectation that the building
 * (or any colony, for that matter) actually exists.
 */
public interface ICraftingBuildingModule extends IBuildingModule
{
    /**
     * Gets the crafting job associated with this building type.
     * This might not be the primary job of the building.
     *
     * Note that this must either always return null or always
     * return a valid job, even if this module is not currently
     * associated with a specific colony or building.  (Note that
     * if it returns null, there will be no JEI integration.)
     *
     * It may either return the actual job associated with a
     * citizen working at the current building, or an abstract
     * job not yet associated with any particular citizen.
     *
     * It is permitted for two crafting module types to return
     * the same job but in that case {@link #getId()} must return
     * different ids.
     *
     * @return The crafting job, or null if there is no such job.
     */
    @Nullable
    IJob<?> getCraftingJob();

    /**
     * Gets an id *suffix* for this particular crafting module, to
     * disambiguate between multiple modules with the same job.
     * This is not unique by itself.
     * @return The disambiguating id suffix.
     */
    @NotNull
    @Deprecated
    String getId();

    /**
     * Gets the unique key for recipes intended for this crafting
     * module in the CustomRecipeManager.
     * Precondition: getCraftingJob() returns non-null.
     * @return The unique key.
     */
    @NotNull
    String getCustomRecipeKey();

    /**
     * Check if the worker can learn a certain type of recipe.
     * @param type the type to check for.
     * @return true if so.
     */
    default boolean canLearn(final CraftingType type)
    {
        return getSupportedCraftingTypes().contains(type);
    }

    /**
     * Get the supported crafting types.
     * @return a set of types.
     */
    Set<CraftingType> getSupportedCraftingTypes();

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
     * Gets a predicate for recipe ingredients that are explicitly
     * allowed or banned at this building.  Used to reduce false
     * negatives from {@link #isRecipeCompatible} when
     * alternatives are available.
     *
     * @return A predicate that returns true when the ingredient
     *         is definitely allowed, false when definitely banned,
     *         or empty when undecided.
     */
    @NotNull
    OptionalPredicate<ItemStack> getIngredientValidator();

    /**
     * Check if the module should have a large limit for learnable recipes.
     * @return true if so.
     */
    boolean canLearnManyRecipes();

    /**
     * Check if the module on the client side should be displayed.
     * @return true if so.
     */
    boolean isVisible();

    /**
     * Get a list of all recipes of this module.
     * @return the list of recipes.
     */
    List<IToken<?>> getRecipes();

    /**
     * Get the first recipe where the output matches this stack.
     * @param stack the output stack to match.
     * @return the recipe or null.
     */
    @Nullable
    IRecipeStorage getFirstRecipe(ItemStack stack);

    /**
     * Check if is the worker has the knowledge to craft something.
     *
     * @param stackPredicate the predicate to check for fullfillment.
     * @return the recipe storage if so.
     */
    @Nullable
    IRecipeStorage getFirstRecipe(Predicate<ItemStack> stackPredicate);

    /**
     * Get a fullfillable recipe to execute.
     *
     * @param stackPredicate the predicate to check for fullfillment.
     * @param count          the count to produce.
     * @param considerReservation if reservations should be considered.
     * @return the recipe or null.
     */
    IRecipeStorage getFirstFulfillableRecipe(Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation);

    /**
     * Fulfill the recipe.
     * @param storage the recipe storage to fulfill.
     * @return true if successful.
     */
    boolean fullFillRecipe(IRecipeStorage storage);

    /**
     * Get tool to use during fulFillRecipe
     * 
     * @param worker the worker to query for tool
     */
    ItemStack getCraftingTool(final AbstractEntityCitizen worker);


    /**
     * Get luck to use during fulFillRecipe
     * 
     * @param worker the worker to calculate luck for
     */
    float getCraftingLuck(final AbstractEntityCitizen worker);

    /**
     * Updates existing requests, if they match the recipes available at this worker
     */
    void updateWorkerAvailableForRecipes();

    /**
     * Replace one current recipe with a new one
     * @param oldRecipe the recipe to replace
     * @param newRecipe the new version
     */
    void replaceRecipe(IToken<?> oldRecipe, IToken<?> newRecipe);

    /**
     * Check if a recipe can be added. This is only important for 3x3 crafting. Workers shall override this if necessary.
     *
     * @param token the token of the recipe.
     * @return true if so.
     */
    boolean canRecipeBeAdded(IToken<?> token);

    /**
     * Add a recipe to the list of recipes.
     *
     * @param token the token to add.
     */
    void addRecipeToList(IToken<?> token, boolean atTop);

    /**
     * Switch the order of two recipes.
     * @param i the first recipe.
     * @param j the second recipe.
     * @param fullMove move it fully up/down.
     */
    void switchOrder(int i, int j, final boolean fullMove);

    /**
     * Generates any additional special recipes supported by this
     * crafter.  Unlike the above, these are not added to the
     * building's learned recipe list; they are only used for
     * display purposes such as the JEI lookup.
     *
     * This is intended for things that the worker AI can do by
     * itself without any explicit recipe (neither taught nor loaded).
     *
     * @return The list of additional recipes.
     */
    @NotNull
    List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull Level world);

    /**
     * Gets a list of loot tables that should be available for drop
     * analysis (beyond any already used in recipes).  This is not
     * intended for actually generating loot, just for display purposes
     * such as in JEI (e.g. via {@link #getAdditionalRecipesForDisplayPurposesOnly(Level)}).
     *
     * @return The list of loot table ids
     */
    @NotNull
    default List<ResourceLocation> getAdditionalLootTables()
    {
        return Collections.emptyList();
    }

    /**
     * Add a recipe to the building.
     *
     * @param token the id of the recipe.
     * @return true if successful
     */
    boolean addRecipe(IToken<?> token);

    /**
     * Remove a recipe of the building.
     *
     * @param token the id of the recipe.
     */
    void removeRecipe(IToken<?> token);

    /**
     * Updates existing requests, if they match the recipes available at this worker
     */
    void checkForWorkerSpecificRecipes();

    /**
     * Clear the list of recipes.
     */
    void clearRecipes();

    /**
     * Randomly improve a certain recipe.
     * @param currentRecipeStorage the recipe to improve.
     * @param craftCounter the craft counter.
     * @param citizenData the citizen running it.
     */
    void improveRecipe(IRecipeStorage currentRecipeStorage, int craftCounter, ICitizenData citizenData);

    /**
     * Gets a unique identifier for this crafting module, based on
     * the job and disambiguation suffix.  May be null where there
     * was no job.
     * @return The unique id or null.
     */
    @Nullable
    @Deprecated
    default ResourceLocation getUid()
    {
        final IJob<?> job = getCraftingJob();
        if (job == null) return null;

        final JobEntry entry = job.getJobRegistryEntry();
        return getUid(entry, getId());
    }

    /**
     * Formats a crafting module unique identifier based on a job entry and disambiguation suffix.
     * @return The unique id.
     */
    @NotNull
    static ResourceLocation getUid(@NotNull final JobEntry job, @NotNull final String id)
    {
        final ResourceLocation jobId = job.getKey();
        return new ResourceLocation(jobId.getNamespace(), jobId.getPath() + "/" + id);
    }

    /**
     * Check if the recipe it part of this module.
     * @param token the recipe token to check.
     * @return true if so.
     */
    boolean holdsRecipe(IToken<?> token);

    /**
     * Enable or disable a recipe at a location.
     * @param recipeLocation the location of the recipe.
     */
    void toggle(int recipeLocation);

    /**
     * Check if a recipe is disabled.
     * @param token the recipe id to check for.
     * @return true if so.
     */
    boolean isDisabled(final IToken<?> token);
}
