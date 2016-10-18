package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobGuard;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Building class of the guard tower.
 */
public class BuildingGuardTower extends AbstractBuildingWorker
{
    /**
     * Name description of the guard hat.
     */
    private static final String GUARD_TOWER = "TowerGuard";

    /**
     * Max level of the guard hut.
     */
    private static final int GUARD_HUT_MAX_LEVEL = 5;

    /**
     * The max vision bonus multiplier
     */
    private static final int MAX_VISION_BONUS_MULTIPLIER = 3;

    /**
     * Vision bonus per level.
     */
    private static final int VISION_BONUS = 5;

    /**
     * Constructor for the guardTower building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingGuardTower(Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Baker schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return GUARD_TOWER;
    }

    /**
     * Gets the max level of the baker's hut.
     *
     * @return The max level of the baker's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return GUARD_HUT_MAX_LEVEL;
    }

    /**
     * The name of the baker's job.
     *
     * @return The name of the baker's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return GUARD_TOWER;
    }

    /**
     * Getter for the bonus vision.
     * @return an integer for the additional range.
     */
    public int getBonusVision()
    {
        if(getBuildingLevel() <= MAX_VISION_BONUS_MULTIPLIER)
        {
            return getBuildingLevel() * VISION_BONUS;
        }
        return MAX_VISION_BONUS_MULTIPLIER * VISION_BONUS;
    }

    /**
     * Create a Baker job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Baker job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobGuard(citizen);
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, GUARD_TOWER);
        }
    }
}


