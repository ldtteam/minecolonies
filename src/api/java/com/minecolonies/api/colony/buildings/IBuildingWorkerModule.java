package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBuildingWorkerModule
{
    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    IJob<?> createJob(ICitizenData citizen);

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
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    String getJobID();

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
}
