package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBuildingWorkerView extends IBuildingView
{
    /**
     * Returns the id of the worker.
     *
     * @return 0 if there is no worker else the correct citizen id.
     */
    List<Integer> getWorkerId();

    /**
     * Sets the id of the worker.
     *
     * @param workerId the id to set.
     */
    void addWorkerId(int workerId);

    /**
     * Get the list of recipes.
     *
     * @return copy of the list.
     */
    List<IRecipeStorage> getRecipes();

    /**
     * Remove a recipe from the list.
     *
     * @param i the index to remove.
     */
    void removeRecipe(int i);

    /**
     * Switch the indices of two recipes.
     *
     * @param i the first.
     * @param j the second.
     */
    void switchIndex(int i, int j);

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
     * Remove a worker from the list.
     *
     * @param id the id to remove.
     */
    void removeWorkerId(int id);

    /**
     * Check if it has enough worker.
     *
     * @return true if so.
     */
    boolean hasEnoughWorkers();

    /**
     * Check if a building can craft complex recipes.
     *
     * @return true if so.
     */
    boolean canCraftComplexRecipes();

    /**
     * Check if an additional recipe can be added.
     *
     * @return true if so.
     */
    boolean canRecipeBeAdded();

    /**
     * Check if players can change the building's recipe list.
     * <p>
     * This is the case for most current buildings, but some buildings might only work on built-in recipes.
     * It's recommended to turn this off for buildings that make no use of player-thaught recipes, to avoid confusion for new players.
     * Turning this on will hide the "Teach recipes" button, hide the remove-buttons in the recipe list,
     * and also hide the recipe list altogether if no recipes are present.
     * </p>
     *
     * @return true if player is allowed to alter  recipes, false if not
     */
    boolean isRecipeAlterationAllowed();

    /**
     * Get the hiring mode of the building.
     *
     * @return the mode.
     */
    HiringMode getHiringMode();

    /**
     * Set the hiring mode and sync to the server.
     *
     * @param hiringMode the mode to set.
     */
    void setHiringMode(HiringMode hiringMode);

    /**
     * Get the name of the job.
     *
     * @return job name.
     */
    String getJobName();
}
