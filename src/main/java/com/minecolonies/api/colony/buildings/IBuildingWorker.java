package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * Get all handlers accociated with this building.
     *
     * @return the handlers of the building + citizen.
     */
    List<IItemHandler> getHandlers();

    boolean assignCitizen(ICitizenData citizen);

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
     *  Recipe Improvement skill getter
     * @return the recipe improvement skill
     */
    @NotNull
    Skill getRecipeImprovementSkill();

    /**
     * Check if the worker is allowed to eat the following stack.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    boolean canEat(final ItemStack stack);
}
