package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Predicate;

public interface IBuildingWorker extends IBuilding
{
    /**
     * Minimal level to ask for wood tools. (WOOD_HUT_LEVEL + 1 == stone)
     */
    int WOOD_HUT_LEVEL = 0;

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    IJob<?> createJob(ICitizenData citizen);

    /**
     * Check if a certain ItemStack is in the request of a worker.
     *
     * @param stack the stack to chest.
     * @return true if so.
     */
    boolean isItemStackInRequest(@Nullable ItemStack stack);

    /**
     * Set a new hiring mode in the building.
     *
     * @param hiringMode the mode to set.
     */
    void setHiringMode(HiringMode hiringMode);

    /**
     * Get the current hiring mode of this building.
     *
     * @return the current mode.
     */
    HiringMode getHiringMode();

    /**
     * Check if is the worker has the knowledge to craft something.
     *
     * @param stack the stack to craft.
     * @return the recipe storage if so.
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
     * @param tempStack the stack which should be crafted.
     * @return the recipe or null.
     */
    IRecipeStorage getFirstFullFillableRecipe(ItemStack tempStack);

    /**
     * Get a fullfillable recipe to execute, with at least a given count.
     *
     * @param tempStack The temp stack to match.
     * @param count     The count to craft.
     * @return The recipe or null.
     */
    IRecipeStorage getFirstFullFillableRecipe(ItemStack tempStack, int count);

    /**
     * Get a fullfillable recipe to execute.
     *
     * @param stackPredicate the predicate to check for fullfillment.
     * @param count          the count to produce.
     * @return the recipe or null.
     */
    IRecipeStorage getFirstFullFillableRecipe(Predicate<ItemStack> stackPredicate, final int count);

    /**
     * Try to fullfill a recipe.
     *
     * @param storage with the storage.
     * @return true if successful.
     */
    boolean fullFillRecipe(IRecipeStorage storage);

    /**
     * Switch indices of two recipes because of the priority.
     *
     * @param i the first index.
     * @param j the second index.
     */
    void switchIndex(int i, int j);

    /**
     * Check if a recipe can be added.
     * This is only important for 3x3 crafting.
     * Workers shall override this if necessary.
     *
     * @param ignored the token of the recipe.
     * @return true if so.
     */
    boolean canRecipeBeAdded(IToken<?> ignored);

    /**
     * Check if players can change the building's recipe list.
     * <p>
     * This is the case for most current buildings, but some buildings might only work on built-in recipes.
     * It's recommended to turn this off for buildings that make no use of player-thaught recipes, to avoid confusion for new players.
     * Turning this on will hide the "Teach recipes" button, hide the remove-buttons in the recipe list,
     * and also hide the recipe list altogether if no recipes are present.
     * </p>
     *
     * @return true if player is allowed to alter recipes, false if not
     */
    boolean isRecipeAlterationAllowed();

    /**
     * Get the list of all recipes the worker can learn.
     *
     * @return a copy of the tokens of the recipes.
     */
    List<IToken<?>> getRecipes();

    /**
     * Get all handlers accociated with this building.
     *
     * @return the handlers of the building + citizen.
     */
    List<IItemHandler> getHandlers();

    boolean assignCitizen(ICitizenData citizen);

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
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    String getJobName();

    /**
     * Get the max tool level useable by the worker.
     *
     * @return the integer.
     */
    int getMaxToolLevel();

    /**
     * Method which defines if a worker should be allowed to work during the rain.
     *
     * @return true if so.
     */
    boolean canWorkDuringTheRain();

    /**
     * Check if a building can craft complex recipes.
     *
     * @return true if so.
     */
    boolean canCraftComplexRecipes();

    /**
     * Primary skill getter.
     *
     * @return the primary skill.
     */
    @NotNull
    Skill getPrimarySkill();

    /**
     * Secondary skill getter.
     *
     * @return the secondary skill.
     */
    @NotNull
    Skill getSecondarySkill();

    /**
     * Check if the worker is allowed to eat the following stack.
     * 
     * @param stack the stack to test.
     * @return true if so.
     */
    boolean canEat(final ItemStack stack);
}
