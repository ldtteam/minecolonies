package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
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
     * @return copy of the list.
     */
    List<IRecipeStorage> getRecipes();

    /**
     * Remove a recipe from the list.
     * @param i the index to remove.
     */
    void removeRecipe(int i);

    /**
     * Switch the indices of two recipes.
     * @param i the first.
     * @param j the second.
     */
    void switchIndex(int i, int j);

    @NotNull
    IBuildingWorker.Skill getPrimarySkill();

    @NotNull
    IBuildingWorker.Skill getSecondarySkill();

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
     * @return true if so.
     */
    boolean canCraftComplexRecipes();

    /**
     * Check if an additional recipe can be added.
     * @return true if so.
     */
    boolean canRecipeBeAdded();

    /**
     * Get the hiring mode of the building.
     * @return the mode.
     */
    HiringMode getHiringMode();

    /**
     * Set the hiring mode and sync to the server.
     * @param hiringMode the mode to set.
     */
    void setHiringMode(HiringMode hiringMode);
}
