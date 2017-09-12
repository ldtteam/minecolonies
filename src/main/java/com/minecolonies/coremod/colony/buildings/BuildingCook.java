package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCook;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the cook building.
 */
public class BuildingCook extends AbstractBuildingWorker
{
    /**
     * The cook string.
     */
    private static final String COOK            = "cook";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Checks if the cook has gathered food at the warehouse today already.
     */
    private boolean hasGatheredToday = false;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COOK;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobCook(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COOK;
    }

    /**
     * Check if the Cook has gathered today already.
     * @return true if so.
     */
    public boolean hasGatheredToday()
    {
        return hasGatheredToday;
    }

    /**
     * Set that the Cook gathered today already.
     */
    public void setGatheredToday()
    {
        hasGatheredToday = true;
    }

    @Override
    public void onWakeUp()
    {
        hasGatheredToday = false;
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiate the cook view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, COOK);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.CHARISMA;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.INTELLIGENCE;
        }
    }
}
