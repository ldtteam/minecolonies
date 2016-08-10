package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobLumberjack;
import net.minecraft.util.BlockPos;

/**
 * The lumberjacks building.
 */
public class BuildingLumberjack extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    private final static int MAX_BUILDING_LEVEL = 3;
    /**
     * The job description.
     */
    private static final String LUMBERJACK          = "Lumberjack";
    /**
     * The hut description.
     */
    private static final String LUMBERJACK_HUT_NAME = "lumberjackHut";

    /**
     * Public constructor of the building, creates an object of the building.
     * @param c the colony.
     * @param l the position.
     */
    public BuildingLumberjack(Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter of the schematic name.
     * @return the schematic name.
     */
    @Override
    public String getSchematicName()
    {
        return LUMBERJACK;
    }

    /**
     * Getter of the max building level.
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * Getter of the job description.
     * @return the description of the lumberjacks job.
     */
    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    /**
     * Create the job for the lumberjack.
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    /**
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Public constructor of the view, creates an instance of it.
         * @param c the colony.
         * @param l the position.
         */
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        /**
         * Gets the blockOut Window.
         * @return the window of the lumberjack building.
         */
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, LUMBERJACK_HUT_NAME);
        }
    }
}
